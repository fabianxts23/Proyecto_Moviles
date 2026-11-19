package cl.duoc.oportunidades.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.oportunidades.data.OportunidadRepository
import cl.duoc.oportunidades.data.OrganizacionDto
import cl.duoc.oportunidades.data.OrganizacionRemotaRepository
import cl.duoc.oportunidades.data.ResultadoOp
import cl.duoc.oportunidades.data.SessionManager
import cl.duoc.oportunidades.data.local.*
import cl.duoc.oportunidades.data.model.*
import cl.duoc.oportunidades.domain.validation.FormularioValidator
import cl.duoc.oportunidades.domain.validation.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



// ---------- Estados de UI (StateFlow) ----------



data class Filtros(
    val texto: String = "",
    val estados: Set<EstadoOportunidad> = emptySet(),
    val prioridades: Set<Prioridad> = emptySet(),
    val tipos: Set<TipoOportunidad> = emptySet(),
    val comuna: String = "",
    val region: String = "",
    val organizacionId: Long? = null
) { val activos: Boolean get() = texto.isNotBlank() || estados.isNotEmpty() ||
        prioridades.isNotEmpty() || tipos.isNotEmpty() || comuna.isNotBlank() ||
        region.isNotBlank() || organizacionId != null }

data class AlertasUi(
    val proximas: List<OportunidadEntity> = emptyList(),
    val sinContacto: List<OportunidadEntity> = emptyList(),
    val propuestasSinRespuesta: List<OportunidadEntity> = emptyList(),
    val sinSeguimiento: List<OportunidadEntity> = emptyList(),
    val atrasadas: List<OportunidadEntity> = emptyList()
) { val total get() = proximas.size + sinContacto.size + propuestasSinRespuesta.size +
        sinSeguimiento.size + atrasadas.size }

data class GuardadoUi(
    val guardando: Boolean = false,
    val exito: String? = null,
    val error: String? = null,
    val errores: Map<String, String> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    val repo = OportunidadRepository(db)
    val session = SessionManager(app)

    init {
        // Garantiza los datos ficticios iniciales (idempotente)
        viewModelScope.launch { repo.asegurarDatosIniciales() }
    }

    // ----- Flujos base de Room -----
    private val todas = repo.oportunidades()
    val organizaciones = repo.organizaciones().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000), emptyList())

    // ----- Filtros de búsqueda -----
    private val _filtros = MutableStateFlow(Filtros())
    val filtros: StateFlow<Filtros> = _filtros

    /** Lista filtrada por texto/estado/prioridad/tipo/comuna/región/organización. */
    val oportunidades = combine(todas, _filtros) { lista, f ->
        lista.filter { o ->
            (f.texto.isBlank() || o.nombre.contains(f.texto, true) ||
                    o.eventoNombre.contains(f.texto, true) ||
                    o.comuna.contains(f.texto, true)) &&
            (f.estados.isEmpty() || o.estado in f.estados) &&
            (f.prioridades.isEmpty() || o.prioridad in f.prioridades) &&
            (f.tipos.isEmpty() || o.tipo in f.tipos) &&
            (f.comuna.isBlank() || o.comuna.contains(f.comuna.trim(), true)) &&
            (f.region.isBlank() || o.region.contains(f.region.trim(), true)) &&
            (f.organizacionId == null || o.organizacionId == f.organizacionId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ----- Alertas -----
    val alertas = combine(
        repo.eventosProximos(), repo.sinContacto(), repo.propuestasSinRespuesta(),
        repo.sinSeguimiento(), repo.gestionesAtrasadas()
    ) { p, sc, psr, ss, atr ->
        // El supervisor ve las alertas del equipo; el ejecutivo, solo las suyas.
        val u = session.usuarioActual
        val visible: (OportunidadEntity) -> Boolean =
            { u == null || u.rol == Rol.SUPERVISOR || it.responsableId == u.id }
        AlertasUi(p.filter(visible), sc.filter(visible), psr.filter(visible),
            ss.filter(visible), atr.filter(visible))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertasUi())

    // ----- Estado de guardado (validaciones) -----
    private val _guardado = MutableStateFlow(GuardadoUi())
    val guardado: StateFlow<GuardadoUi> = _guardado




    fun limpiarGuardado() { _guardado.value = GuardadoUi() }

    /** Oculta solo el banner de éxito/error; los errores bajo cada campo se conservan. */
    fun limpiarMensaje() { _guardado.value = _guardado.value.copy(exito = null, error = null) }

    // ---------- Filtros ----------
    fun actualizarFiltros(f: Filtros) { _filtros.value = f }
    fun limpiarFiltros() { _filtros.value = Filtros() }

    // ---------- CRUD oportunidad ----------
    fun guardarOportunidad(
        existente: OportunidadEntity?,
        campos: Map<String, String>,
        tipo: TipoOportunidad?,
        fuente: FuenteInformacion?,
        prioridad: Prioridad,
        organizacionId: Long?
    ) {
        val deteccion = existente?.fechaDeteccion ?: System.currentTimeMillis()
        val errores = validarFormulario(campos, deteccion).toMutableMap()
        if (tipo == null) errores["tipo"] = "Seleccione el tipo de oportunidad"
        if (fuente == null) errores["fuente"] = "Seleccione la fuente de información"
        if (organizacionId == null) errores["organizacion"] = "Seleccione la organización responsable"
        if (errores.isNotEmpty() || tipo == null || fuente == null || organizacionId == null) {
            // Se mantienen los valores ingresados (viven en la pantalla)
            _guardado.value = GuardadoUi(
                error = FormularioValidator.resumen(errores) ?: "Revise los campos marcados en rojo",
                errores = errores)
            return
        }
        val usuarioId = session.usuarioActual?.id
        if (usuarioId == null) {
            _guardado.value = GuardadoUi(error = "Sesión no válida. Vuelva a iniciar sesión.")
            return
        }
        _guardado.value = GuardadoUi(guardando = true)
        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            val base = existente ?: OportunidadEntity(
                nombre = "", tipo = tipo, descripcion = "", fuente = fuente,
                fechaDeteccion = ahora, eventoNombre = "", eventoTipo = "",
                fechaInicio = ahora, fechaTermino = ahora, horario = "",
                direccion = "", comuna = "", region = "", organizacionId = organizacionId,
                publicoEsperado = "", publicoObjetivo = "",
                cantidadAsistentesEstimada = 0, condiciones = "", requisitos = "",
                costosReferenciales = 0.0, espaciosDisponibles = "",
                prioridad = prioridad, potencialComercial = 1,
                estado = EstadoOportunidad.EVENTO_IDENTIFICADO,
                responsableId = usuarioId
            )
            val entidad = base.copy(
                nombre = campos["nombre"]!!.trim(),
                tipo = tipo,
                descripcion = campos["descripcion"]!!.trim(),
                fuente = fuente,
                eventoNombre = campos["eventoNombre"]!!.trim(),
                eventoTipo = campos["eventoTipo"]!!.trim(),
                fechaInicio = fechaALong(campos["fechaInicio"]!!),
                fechaTermino = fechaALong(campos["fechaTermino"]!!),
                horario = campos["horario"]!!.trim(),
                direccion = campos["direccion"]!!.trim(),
                comuna = campos["comuna"]!!.trim(),
                region = campos["region"]!!.trim(),
                organizacionId = organizacionId,
                publicoEsperado = campos["publicoEsperado"]!!.trim(),
                publicoObjetivo = campos["publicoObjetivo"]!!.trim(),
                cantidadAsistentesEstimada = campos["asistentes"]!!.trim().toInt(),
                condiciones = campos["condiciones"]!!.trim(),
                requisitos = campos["requisitos"]!!.trim(),
                costosReferenciales = campos["costos"]!!.trim().toDouble(),
                espaciosDisponibles = campos["espacios"]!!.trim(),
                prioridad = prioridad,
                potencialComercial = campos["potencial"]!!.trim().toInt()
            )
            try {
                if (existente == null) repo.crearOportunidad(entidad)
                else repo.actualizarOportunidad(entidad)
                _guardado.value = GuardadoUi(exito = "Oportunidad guardada correctamente")
            } catch (e: Exception) {
                _guardado.value = GuardadoUi(error = "No se pudo guardar: ${e.message}")
            }
        }
    }

    /** Validación de todo el formulario (reglas de negocio incluidas). */
    fun validarFormulario(c: Map<String, String>, fechaDeteccion: Long): Map<String, String> =
        FormularioValidator.oportunidad(c, fechaDeteccion)

    // ---------- Validación de contacto (formulario de organizaciones) ----------
    fun validarContacto(c: Map<String, String>): Map<String, String> =
        FormularioValidator.contacto(c)

    fun guardarContacto(c: ContactoEntity, esNuevo: Boolean) {
        _guardado.value = GuardadoUi(guardando = true)
        viewModelScope.launch {
            try {
                repo.crearContacto(c)
                _guardado.value = GuardadoUi(exito = "Contacto guardado correctamente")
            } catch (e: Exception) {
                _guardado.value = GuardadoUi(error = "No se pudo guardar el contacto")
            }
        }
    }

    private val organizacionesRemotas = OrganizacionRemotaRepository()

    fun guardarOrganizacion(o: OrganizacionEntity) {
        _guardado.value = GuardadoUi(guardando = true)
        viewModelScope.launch {
            try {
                repo.crearOrganizacion(o)
            } catch (e: Exception) {
                _guardado.value = GuardadoUi(error = "No se pudo guardar la organización")
                return@launch
            }
            // Se intenta enviar también al backend; si no hay conexión queda solo en el celular
            val enviada = organizacionesRemotas.enviar(OrganizacionDto.desde(o))
            _guardado.value = GuardadoUi(exito = if (enviada)
                "Organización guardada correctamente"
            else "Organización guardada solo en el celular (sin conexión con el servidor)")
        }
    }

    /**
     * Trae las organizaciones del backend (por ejemplo, las creadas desde Swagger) y agrega
     * a la app las que falten. Si el servidor no responde, no cambia nada.
     */
    fun sincronizarOrganizaciones() {
        viewModelScope.launch {
            val remotas = organizacionesRemotas.descargar() ?: return@launch
            try {
                repo.importarOrganizaciones(remotas.map { it.aEntidadLocal() })
            } catch (e: Exception) {
                // La lista local sigue funcionando aunque falle la sincronización
            }
        }
    }

    // ---------- Acciones de gestión ----------
    fun cambiarEstado(id: Long, nuevo: EstadoOportunidad) {
        viewModelScope.launch {
            when (val r = repo.cambiarEstado(id, nuevo, session.rol ?: Rol.EJECUTIVO)) {
                is ResultadoOp.Ok -> _guardado.value =
                    GuardadoUi(exito = "Estado actualizado a ${nuevo.etiqueta}")
                is ResultadoOp.Error -> _guardado.value = GuardadoUi(error = r.mensaje)
            }
        }
    }

    fun registrarSeguimiento(s: SeguimientoEntity) {
        viewModelScope.launch {
            try {
                repo.agregarSeguimiento(s)
                _guardado.value = GuardadoUi(exito = "Gestión registrada")
            } catch (e: Exception) {
                _guardado.value = GuardadoUi(error = "No se pudo registrar la gestión")
            }
        }
    }

    fun guardarFoto(opId: Long, uri: android.net.Uri) {
        viewModelScope.launch { repo.guardarFoto(opId, uri.toString()) }
    }

    // ---------- Acciones exclusivas del SUPERVISOR ----------
    fun asignar(id: Long, ejecutivo: UsuarioEntity) {
        viewModelScope.launch {
            when (val r = repo.asignarResponsable(id, ejecutivo.id, session.rol ?: Rol.EJECUTIVO)) {
                is ResultadoOp.Ok -> _guardado.value =
                    GuardadoUi(exito = "Responsable asignado: ${ejecutivo.nombre}")
                is ResultadoOp.Error -> _guardado.value = GuardadoUi(error = r.mensaje)
            }
        }
    }

    fun cambiarPrioridad(id: Long, p: Prioridad) {
        viewModelScope.launch {
            when (val r = repo.cambiarPrioridad(id, p, session.rol ?: Rol.EJECUTIVO)) {
                is ResultadoOp.Ok -> _guardado.value =
                    GuardadoUi(exito = "Prioridad actualizada a ${p.etiqueta}")
                is ResultadoOp.Error -> _guardado.value = GuardadoUi(error = r.mensaje)
            }
        }
    }

    fun validarOdescartar(id: Long, confirmar: Boolean) {
        if (session.rol != Rol.SUPERVISOR) {
            _guardado.value = GuardadoUi(error = "Acción bloqueada: solo el supervisor puede confirmar o descartar")
            return
        }
        cambiarEstado(id, if (confirmar)
            EstadoOportunidad.PARTICIPACION_CONFIRMADA
        else EstadoOportunidad.OPORTUNIDAD_DESCARTADA)
    }

    // ---------- Utilidades públicas para pantallas ----------
    fun mostrarErrorPublico(mensaje: String) {
        _guardado.value = GuardadoUi(error = mensaje)
    }

    /**
     * Registra el resultado de una activación. El estado pasa a ACTIVACION_REALIZADA
     * (estado final) junto con las métricas ingresadas.
     */
    fun registrarResultado(id: Long, datos: Map<String, String>) {
        viewModelScope.launch {
            val o = repo.oportunidadSync(id) ?: return@launch
            val evaluacion = datos["evaluacion"]?.toIntOrNull()?.coerceIn(1, 5)
            try {
                repo.actualizarOportunidad(o.copy(
                    estado = EstadoOportunidad.ACTIVACION_REALIZADA,
                    resEstadoFinal = "Exitosa",
                    resFechaRealizacion = System.currentTimeMillis(),
                    resAsistentes = datos["asistentes"]?.toIntOrNull(),
                    resContactosGenerados = datos["contactos"]?.toIntOrNull(),
                    resPersonasInteresadas = datos["interesados"]?.toIntOrNull(),
                    resInscripciones = datos["inscripciones"]?.toIntOrNull(),
                    resEvaluacion = evaluacion,
                    resObservaciones = datos["observaciones"]?.takeIf { it.isNotBlank() },
                    resAprendizajes = datos["aprendizajes"]?.takeIf { it.isNotBlank() }
                ))
                _guardado.value = GuardadoUi(exito = "Resultado registrado")
            } catch (e: Exception) {
                _guardado.value = GuardadoUi(error = "No se pudo registrar el resultado")
            }
        }
    }

    // ---------- Detalle ----------
    fun oportunidad(id: Long) = repo.oportunidad(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun seguimientos(id: Long) = repo.seguimientos(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fotos(id: Long) = repo.fotos(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun contactos(orgId: Long) = repo.contactos(orgId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Utilidades de fecha ----------
    companion object {
        private val FORMATO = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        fun fechaALong(fecha: String): Long =
            try { FORMATO.parse(fecha)?.time ?: System.currentTimeMillis() }
            catch (e: Exception) { System.currentTimeMillis() }
        fun longAFecha(t: Long): String = FORMATO.format(java.util.Date(t))

        /** Medianoche (00:00) del día al que pertenece el instante dado. */
        fun inicioDelDia(t: Long): Long = java.util.Calendar.getInstance().apply {
            timeInMillis = t
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

}
