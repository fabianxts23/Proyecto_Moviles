package cl.duoc.oportunidades.data

import cl.duoc.oportunidades.data.local.*
import cl.duoc.oportunidades.data.model.*
import kotlinx.coroutines.flow.Flow

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

    suspend fun ejecutivos(): List<UsuarioEntity> = db.usuarioDao().listarEjecutivos()

    // ----- Organizaciones y contactos -----
    fun organizaciones(): Flow<List<OrganizacionEntity>> = db.organizacionDao().observarTodas()
    fun organizacion(id: Long): Flow<OrganizacionEntity?> = db.organizacionDao().observarPorId(id)
    suspend fun crearOrganizacion(o: OrganizacionEntity): Long = db.organizacionDao().insertar(o)

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
        if (actual.estado.esFinal) return ResultadoOp.Error("La oportunidad ya está cerrada")
        val permitidos = ReglasEstado.transicionesPermitidas(actual.estado, rol)
        if (nuevo !in permitidos) {
            return ResultadoOp.Error("Transición no permitida para el rol ${rol.name.lowercase()}")
        }
        db.oportunidadDao().actualizar(actual.copy(estado = nuevo,
            fechaUltimaActualizacion = System.currentTimeMillis()))
        return ResultadoOp.Ok
    }

    suspend fun asignarResponsable(id: Long, ejecutivoId: Long): ResultadoOp {
        val actual = db.oportunidadDao().porId(id) ?: return ResultadoOp.Error("No existe")
        db.oportunidadDao().actualizar(actual.copy(responsableId = ejecutivoId,
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
