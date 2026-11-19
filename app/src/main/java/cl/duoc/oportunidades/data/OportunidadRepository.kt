package cl.duoc.oportunidades.data

import cl.duoc.oportunidades.data.local.*
import cl.duoc.oportunidades.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Evita que dos sincronizaciones simultáneas inserten la misma organización dos veces. */
private val candadoSyncOrganizaciones = Mutex()

/**
 * Repositorio: única puerta de acceso a los datos (capa MVVM).
 * La UI nunca habla directamente con Room.
 */
class OportunidadRepository(private val db: AppDatabase) {

    private val dias = 24L * 60 * 60 * 1000

    // ----- Autenticación -----
    /** Garantiza que los datos ficticios iniciales existan (idempotente). */
    suspend fun asegurarDatosIniciales() = SeedData.asegurar(db)

    suspend fun login(correo: String, password: String): UsuarioEntity? {
        asegurarDatosIniciales()
        return db.usuarioDao().login(correo.trim().lowercase(), password)
    }

    /**
     * Refleja en Room un usuario autenticado por el backend. Se busca por correo para conservar
     * el id local (las oportunidades referencian ese id con clave foránea).
     */
    suspend fun sincronizarUsuario(remoto: UsuarioEntity): UsuarioEntity {
        asegurarDatosIniciales()
        val dao = db.usuarioDao()
        val local = dao.porCorreo(remoto.correo)
        return if (local == null) {
            remoto.copy(id = dao.insertar(remoto.copy(id = 0)))
        } else {
            remoto.copy(id = local.id).also { dao.actualizar(it) }
        }
    }

    suspend fun ejecutivos(): List<UsuarioEntity> = db.usuarioDao().listarEjecutivos()

    /** Todos los usuarios activos (para mostrar quién es el responsable de cada gestión). */
    suspend fun usuarios(): List<UsuarioEntity> = db.usuarioDao().listarActivos()

    suspend fun usuario(id: Long): UsuarioEntity? = db.usuarioDao().porId(id)

    // ----- Organizaciones y contactos -----
    fun organizaciones(): Flow<List<OrganizacionEntity>> = db.organizacionDao().observarTodas()
    fun organizacion(id: Long): Flow<OrganizacionEntity?> = db.organizacionDao().observarPorId(id)
    suspend fun crearOrganizacion(o: OrganizacionEntity): Long = db.organizacionDao().insertar(o)

    /**
     * Guarda en la base local las organizaciones del backend que la app aún no tiene.
     * Retorna cuántas se agregaron.
     */
    suspend fun importarOrganizaciones(remotas: List<OrganizacionEntity>): Int =
        candadoSyncOrganizaciones.withLock {
            val dao = db.organizacionDao()
            val nuevas = OrganizacionSync.nuevas(remotas, { it.nombre }, dao.nombres())
            nuevas.forEach { dao.insertar(it) }
            nuevas.size
        }

    fun contactos(orgId: Long): Flow<List<ContactoEntity>> =
        db.contactoDao().observarPorOrganizacion(orgId)

    suspend fun crearContacto(c: ContactoEntity): Long = db.contactoDao().insertar(c)

    // ----- Oportunidades (CRUD real) -----
    fun oportunidades(): Flow<List<OportunidadEntity>> = db.oportunidadDao().observarTodas()
    fun oportunidad(id: Long): Flow<OportunidadEntity?> = db.oportunidadDao().observarPorId(id)
    suspend fun oportunidadSync(id: Long): OportunidadEntity? = db.oportunidadDao().porId(id)

    suspend fun crearOportunidad(o: OportunidadEntity): Long = db.oportunidadDao().insertar(o)

    suspend fun actualizarOportunidad(o: OportunidadEntity) =
        db.oportunidadDao().actualizar(o.copy(fechaUltimaActualizacion = System.currentTimeMillis()))

    /** Cambio de estado respetando reglas de negocio (rol y transiciones). */
    suspend fun cambiarEstado(id: Long, nuevo: EstadoOportunidad, rol: Rol): ResultadoOp {
        val actual = db.oportunidadDao().porId(id) ?: return ResultadoOp.Error("No existe la oportunidad")
        // Regla de negocio: cerrada = sin cambios; finales = solo supervisor.
        ReglasEstado.validar(actual.estado, nuevo, rol)?.let { return ResultadoOp.Error(it) }
        val ahora = System.currentTimeMillis()
        db.oportunidadDao().actualizar(actual.copy(
            estado = nuevo,
            // Al pasar a "Propuesta enviada" se registra la fecha para la alerta de respuesta
            propuestaEnviada = actual.propuestaEnviada || nuevo == EstadoOportunidad.PROPUESTA_ENVIADA,
            fechaPropuesta = if (nuevo == EstadoOportunidad.PROPUESTA_ENVIADA) ahora
                else actual.fechaPropuesta,
            fechaUltimaActualizacion = ahora))
        return ResultadoOp.Ok
    }

    /** Asigna o reasigna el responsable. Solo supervisor y solo oportunidades activas. */
    suspend fun asignarResponsable(id: Long, ejecutivoId: Long, rol: Rol): ResultadoOp {
        if (rol != Rol.SUPERVISOR) {
            return ResultadoOp.Error("Acción bloqueada: solo el supervisor puede asignar responsables")
        }
        val actual = db.oportunidadDao().porId(id) ?: return ResultadoOp.Error("No existe la oportunidad")
        if (actual.estado.esFinal) {
            return ResultadoOp.Error("La oportunidad está cerrada y no admite reasignación")
        }
        val ejecutivo = db.usuarioDao().porId(ejecutivoId)
        if (ejecutivo == null || ejecutivo.rol != Rol.EJECUTIVO || !ejecutivo.cuentaActiva) {
            return ResultadoOp.Error("El ejecutivo seleccionado no está disponible")
        }
        if (actual.responsableId == ejecutivoId) {
            return ResultadoOp.Error("${ejecutivo.nombre} ya es el responsable de esta oportunidad")
        }
        db.oportunidadDao().actualizar(actual.copy(responsableId = ejecutivoId,
            fechaUltimaActualizacion = System.currentTimeMillis()))
        return ResultadoOp.Ok
    }

    /** Cambia la prioridad. Solo supervisor y solo oportunidades activas. */
    suspend fun cambiarPrioridad(id: Long, nueva: Prioridad, rol: Rol): ResultadoOp {
        if (rol != Rol.SUPERVISOR) {
            return ResultadoOp.Error("Acción bloqueada: solo el supervisor puede cambiar la prioridad")
        }
        val actual = db.oportunidadDao().porId(id) ?: return ResultadoOp.Error("No existe la oportunidad")
        if (actual.estado.esFinal) {
            return ResultadoOp.Error("La oportunidad está cerrada y su prioridad no se puede modificar")
        }
        if (actual.prioridad == nueva) {
            return ResultadoOp.Error("La oportunidad ya tiene prioridad ${nueva.etiqueta}")
        }
        db.oportunidadDao().actualizar(actual.copy(prioridad = nueva,
            fechaUltimaActualizacion = System.currentTimeMillis()))
        return ResultadoOp.Ok
    }

    // ----- Seguimiento (historial nunca se borra) -----
    fun seguimientos(opId: Long): Flow<List<SeguimientoEntity>> =
        db.seguimientoDao().observarPorOportunidad(opId)

    suspend fun agregarSeguimiento(s: SeguimientoEntity): Long =
        db.seguimientoDao().insertar(s)

    // ----- Alertas -----
    fun eventosProximos(): Flow<List<OportunidadEntity>> =
        db.oportunidadDao().observarEventosProximos(
            System.currentTimeMillis(), System.currentTimeMillis() + 14 * dias)

    fun sinContacto(): Flow<List<OportunidadEntity>> = db.oportunidadDao().observarSinContacto()

    fun propuestasSinRespuesta(): Flow<List<OportunidadEntity>> =
        db.oportunidadDao().observarPropuestasSinRespuesta(
            System.currentTimeMillis() - 7 * dias)

    fun sinSeguimiento(): Flow<List<OportunidadEntity>> =
        db.oportunidadDao().observarSinSeguimiento()

    fun gestionesAtrasadas(): Flow<List<OportunidadEntity>> =
        db.oportunidadDao().observarGestionesAtrasadas(System.currentTimeMillis())

    // ----- Evidencias fotográficas -----
    fun fotos(opId: Long): Flow<List<EvidenciaFotoEntity>> =
        db.evidenciaFotoDao().observarPorOportunidad(opId)

    suspend fun guardarFoto(opId: Long, uri: String) =
        db.evidenciaFotoDao().insertar(EvidenciaFotoEntity(oportunidadId = opId, uri = uri))
}

sealed class ResultadoOp {
    object Ok : ResultadoOp()
    data class Error(val mensaje: String) : ResultadoOp()
}
