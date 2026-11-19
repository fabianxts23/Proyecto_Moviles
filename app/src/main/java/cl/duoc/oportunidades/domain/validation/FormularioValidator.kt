package cl.duoc.oportunidades.domain.validation

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Validación de formularios completos, sin depender de Android (se puede probar en la JVM).
 * Cada función recibe los valores escritos por el usuario y retorna un mapa
 * campo -> mensaje de error. Un mapa vacío significa que el formulario es válido.
 */
object FormularioValidator {

    const val MAX_NOMBRE = 100
    const val MAX_TEXTO = 200
    /** Comuna y región: el backend las limita a 80 caracteres. */
    const val MAX_UBICACION = 80
    const val MAX_SITIO_WEB = 200
    const val MAX_TEXTO_LARGO = 500

    /** Nombre legible de cada campo, usado en el aviso "Faltan: ...". */
    private val ETIQUETAS = mapOf(
        "nombre" to "Nombre", "descripcion" to "Descripción",
        "tipo" to "Tipo de oportunidad", "fuente" to "Fuente de información",
        "eventoNombre" to "Nombre del evento", "eventoTipo" to "Tipo de evento",
        "fechaInicio" to "Fecha de inicio", "fechaTermino" to "Fecha de término",
        "horario" to "Horario", "direccion" to "Dirección", "comuna" to "Comuna",
        "region" to "Región", "organizacion" to "Organización",
        "asistentes" to "Público esperado", "publicoEsperado" to "Perfil del público",
        "publicoObjetivo" to "Público objetivo", "condiciones" to "Condiciones",
        "requisitos" to "Requisitos", "costos" to "Costos", "espacios" to "Espacios",
        "potencial" to "Potencial comercial",
        "cargo" to "Cargo", "correo" to "Correo", "telefono" to "Teléfono",
        "rubro" to "Rubro", "sitioWeb" to "Sitio web", "publico" to "Público asociado",
        "observaciones" to "Observaciones", "contactos" to "Contactos generados",
        "interesados" to "Personas interesadas", "inscripciones" to "Inscripciones",
        "evaluacion" to "Evaluación", "aprendizajes" to "Aprendizajes",
        "fechaAccion" to "Fecha de la gestión", "horaAccion" to "Hora de la gestión",
        "resultado" to "Resultado", "proximaAccion" to "Próxima acción",
        "fechaProxima" to "Fecha del próximo seguimiento", "clave" to "Contraseña"
    )

    /**
     * Mensaje de aviso para mostrar al usuario cuando faltan datos, por ejemplo:
     * "Revise 3 campos: Nombre, Región y Horario".
     */
    fun resumen(errores: Map<String, String>): String? {
        if (errores.isEmpty()) return null
        val nombres = errores.keys.map { ETIQUETAS[it] ?: it }
        val lista = when (nombres.size) {
            1 -> nombres[0]
            2 -> "${nombres[0]} y ${nombres[1]}"
            3 -> "${nombres[0]}, ${nombres[1]} y ${nombres[2]}"
            else -> "${nombres[0]}, ${nombres[1]}, ${nombres[2]} y ${nombres.size - 3} más"
        }
        return if (nombres.size == 1) "Revise el campo: $lista"
        else "Revise ${nombres.size} campos: $lista"
    }

    // ---------- Inicio de sesión ----------
    fun login(correo: String, clave: String): Map<String, String> {
        val e = mutableMapOf<String, String>()
        Validators.email(correo.trim())?.let { e["correo"] = it }
        if (clave.isBlank()) e["clave"] = "Ingrese la contraseña"
        return e
    }

    // ---------- Oportunidad ----------
    fun oportunidad(c: Map<String, String>, fechaDeteccion: Long): Map<String, String> {
        val e = mutableMapOf<String, String>()
        fun texto(clave: String, etiqueta: String, max: Int) {
            Validators.validar(
                Validators.requerido(c[clave], etiqueta),
                Validators.longitudMaxima(c[clave], max, etiqueta)
            )?.let { e[clave] = it }
        }
        texto("nombre", "El nombre", MAX_NOMBRE)
        texto("descripcion", "La descripción", MAX_TEXTO_LARGO)
        texto("eventoNombre", "El nombre del evento", MAX_NOMBRE)
        texto("eventoTipo", "El tipo de evento", MAX_NOMBRE)
        Validators.fechaValida(c["fechaInicio"] ?: "")?.let { e["fechaInicio"] = it }
        Validators.fechaValida(c["fechaTermino"] ?: "")?.let { e["fechaTermino"] = it }
        Validators.horario(c["horario"] ?: "")?.let { e["horario"] = it }
        texto("direccion", "La dirección", MAX_TEXTO)
        texto("comuna", "La comuna", MAX_UBICACION)
        texto("region", "La región", MAX_UBICACION)
        texto("publicoEsperado", "El perfil del público esperado", MAX_TEXTO)
        texto("publicoObjetivo", "El público objetivo", MAX_TEXTO)
        Validators.enteroNoNegativo(c["asistentes"] ?: "", "El público esperado")
            ?.let { e["asistentes"] = it }
        texto("condiciones", "Las condiciones", MAX_TEXTO_LARGO)
        texto("requisitos", "Los requisitos", MAX_TEXTO_LARGO)
        Validators.costoNoNegativo(c["costos"] ?: "")?.let { e["costos"] = it }
        texto("espacios", "Los espacios", MAX_TEXTO)
        Validators.potencial(c["potencial"] ?: "")?.let { e["potencial"] = it }

        // Reglas de negocio: coherencia de fechas
        //  - el inicio no puede ser anterior a la fecha de detección
        //  - el término no puede ser anterior al inicio
        val inicioTxt = c["fechaInicio"]?.trim()
        val terminoTxt = c["fechaTermino"]?.trim()
        if (!e.containsKey("fechaInicio") && inicioTxt != null) {
            if (fechaALong(inicioTxt) < inicioDelDia(fechaDeteccion)) {
                e["fechaInicio"] = "El evento no puede iniciar antes de ser detectado (" +
                        longAFecha(fechaDeteccion) + ")"
            }
        }
        if (!e.containsKey("fechaInicio") && !e.containsKey("fechaTermino") &&
            inicioTxt != null && terminoTxt != null &&
            fechaALong(terminoTxt) < fechaALong(inicioTxt)
        ) {
            e["fechaTermino"] = "La fecha de término no puede ser anterior al inicio"
        }
        return e
    }

    // ---------- Contacto ----------
    fun contacto(c: Map<String, String>): Map<String, String> {
        val e = mutableMapOf<String, String>()
        Validators.validar(
            Validators.requerido(c["nombre"], "El nombre"),
            Validators.longitudMaxima(c["nombre"], MAX_NOMBRE, "El nombre")
        )?.let { e["nombre"] = it }
        Validators.validar(
            Validators.requerido(c["cargo"], "El cargo"),
            Validators.longitudMaxima(c["cargo"], MAX_NOMBRE, "El cargo")
        )?.let { e["cargo"] = it }
        Validators.email(c["correo"] ?: "")?.let { e["correo"] = it }
        Validators.telefono(c["telefono"] ?: "")?.let { e["telefono"] = it }
        return e
    }

    // ---------- Organización ----------
    fun organizacion(c: Map<String, String>): Map<String, String> {
        val e = mutableMapOf<String, String>()
        fun obligatorio(clave: String, etiqueta: String, max: Int) {
            Validators.validar(
                Validators.requerido(c[clave], etiqueta),
                Validators.longitudMaxima(c[clave], max, etiqueta)
            )?.let { e[clave] = it }
        }
        obligatorio("nombre", "El nombre", MAX_NOMBRE)
        obligatorio("rubro", "El rubro", MAX_NOMBRE)
        obligatorio("comuna", "La comuna", MAX_UBICACION)
        obligatorio("region", "La región", MAX_UBICACION)
        Validators.validar(
            Validators.sitioWebOpcional(c["sitioWeb"]),
            Validators.longitudMaxima(c["sitioWeb"], MAX_SITIO_WEB, "El sitio web")
        )?.let { e["sitioWeb"] = it }
        Validators.longitudMaxima(c["publico"], MAX_TEXTO, "El público asociado")
            ?.let { e["publico"] = it }
        Validators.longitudMaxima(c["observaciones"], MAX_TEXTO_LARGO, "Las observaciones")
            ?.let { e["observaciones"] = it }
        return e
    }

    // ---------- Resultado de la activación ----------
    fun resultado(c: Map<String, String>): Map<String, String> {
        val e = mutableMapOf<String, String>()
        Validators.enteroNoNegativo(c["asistentes"] ?: "", "Los asistentes")
            ?.let { e["asistentes"] = it }
        Validators.enteroNoNegativo(c["contactos"] ?: "", "Los contactos generados")
            ?.let { e["contactos"] = it }
        Validators.enteroOpcional(c["interesados"], "Las personas interesadas")
            ?.let { e["interesados"] = it }
        Validators.enteroOpcional(c["inscripciones"], "Las inscripciones")
            ?.let { e["inscripciones"] = it }
        Validators.evaluacionOpcional(c["evaluacion"])?.let { e["evaluacion"] = it }
        Validators.longitudMaxima(c["observaciones"], MAX_TEXTO_LARGO, "Las observaciones")
            ?.let { e["observaciones"] = it }
        Validators.longitudMaxima(c["aprendizajes"], MAX_TEXTO_LARGO, "Los aprendizajes")
            ?.let { e["aprendizajes"] = it }
        return e
    }

    // ---------- Seguimiento (nueva gestión) ----------
    fun seguimiento(c: Map<String, String>): Map<String, String> {
        val e = mutableMapOf<String, String>()
        Validators.fechaValida(c["fechaAccion"] ?: "")?.let { e["fechaAccion"] = it }
        Validators.hora(c["horaAccion"] ?: "")?.let { e["horaAccion"] = it }
        Validators.validar(
            Validators.requerido(c["resultado"], "El resultado"),
            Validators.longitudMaxima(c["resultado"], MAX_TEXTO_LARGO, "El resultado")
        )?.let { e["resultado"] = it }
        Validators.validar(
            Validators.requerido(c["proximaAccion"], "La próxima acción"),
            Validators.longitudMaxima(c["proximaAccion"], MAX_TEXTO, "La próxima acción")
        )?.let { e["proximaAccion"] = it }
        Validators.fechaValida(c["fechaProxima"] ?: "")?.let { e["fechaProxima"] = it }
        if (!e.containsKey("fechaAccion") && !e.containsKey("fechaProxima") &&
            fechaALong((c["fechaProxima"] ?: "").trim()) < fechaALong((c["fechaAccion"] ?: "").trim())
        ) {
            e["fechaProxima"] = "La próxima fecha no puede ser anterior a la fecha de la acción"
        }
        return e
    }

    // ---------- Utilidades de fecha (sin Android) ----------
    private fun formato() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun fechaALong(fecha: String): Long =
        try { formato().parse(fecha)?.time ?: System.currentTimeMillis() }
        catch (ex: Exception) { System.currentTimeMillis() }

    private fun longAFecha(t: Long): String = formato().format(java.util.Date(t))

    private fun inicioDelDia(t: Long): Long = Calendar.getInstance().apply {
        timeInMillis = t
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
