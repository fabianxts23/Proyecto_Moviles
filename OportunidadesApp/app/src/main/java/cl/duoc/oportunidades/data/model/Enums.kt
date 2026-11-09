package cl.duoc.oportunidades.data.model

/** Roles de la aplicación (control de acceso por rol). */
enum class Rol { EJECUTIVO, SUPERVISOR, MARKETING }

/** Tipos de oportunidad definidos en el caso (punto 3.2). */
enum class TipoOportunidad(val etiqueta: String) {
    EVENTO_CORPORATIVO("Evento corporativo"),
    FERIA_EMPRESARIAL("Feria empresarial"),
    ACTIVIDAD_MUNICIPAL("Actividad municipal"),
    JORNADA_BENEFICIOS("Jornada de beneficios"),
    FERIA_INSTITUCIONAL("Feria institucional"),
    ACTIVIDAD_TRABAJADORES("Actividad de trabajadores"),
    ACTIVACION_COMERCIAL("Activación comercial"),
    OTRA("Otra instancia")
}

/** Nivel de prioridad (se persiste como ordinal para poder ordenar). */
enum class Prioridad(val etiqueta: String) {
    BAJA("Baja"), MEDIA("Media"), ALTA("Alta"), CRITICA("Crítica")
}

/**
 * Estados de la oportunidad (anexo + estados extra para trazabilidad).
 * Los estados marcados como final solo los puede fijar el SUPERVISOR.
 */
enum class EstadoOportunidad(val etiqueta: String, val esFinal: Boolean = false) {
    EVENTO_IDENTIFICADO("Evento identificado"),
    CONTACTO_PENDIENTE("Contacto pendiente"),
    CONTACTO_REALIZADO("Contacto realizado"),
    PROPUESTA_ENVIADA("Propuesta enviada"),
    EN_ESPERA_RESPUESTA("En espera de respuesta"),
    EN_EVALUACION("En evaluación"),
    INFORMACION_INCOMPLETA("Información incompleta"),
    REQUIERE_SEGUIMIENTO("Requiere seguimiento"),
    PARTICIPACION_CONFIRMADA("Participación confirmada", esFinal = true),
    OPORTUNIDAD_DESCARTADA("Oportunidad descartada", esFinal = true),
    ACTIVACION_REALIZADA("Activación realizada", esFinal = true),
    ACTIVACION_CANCELADA("Activación cancelada", esFinal = true)
}

enum class TipoOrganizacion(val etiqueta: String) {
    MUNICIPALIDAD("Municipalidad"),
    INSTITUCION_PUBLICA("Institución pública"),
    INSTITUCION_PRIVADA("Institución privada"),
    ORGANIZADOR_EVENTOS("Organizador de eventos"),
    CENTRO_COMERCIAL("Centro comercial"),
    ASOCIACION("Asociación"),
    OTRA("Otra")
}

/** Áreas de contacto definidas en el anexo (punto 3.2). */
enum class AreaContacto(val etiqueta: String) {
    EVENTOS("Eventos"),
    ACTIVACIONES("Activaciones"),
    MARKETING("Marketing"),
    COMUNICACIONES("Comunicaciones"),
    RECURSOS_HUMANOS("Recursos Humanos"),
    BIENESTAR("Bienestar"),
    BENEFICIOS("Beneficios")
}

/** Tipos de acción de seguimiento (anexo, punto 3.2). */
enum class TipoAccion(val etiqueta: String) {
    INVESTIGACION_INICIAL("Investigación inicial"),
    BUSQUEDA_CONTACTO("Búsqueda de contacto"),
    PRIMER_CONTACTO("Primer contacto"),
    ENVIO_PRESENTACION("Envío de presentación"),
    ENVIO_PROPUESTA("Envío de propuesta"),
    SOLICITUD_ANTECEDENTES("Solicitud de antecedentes"),
    REUNION("Reunión"),
    LLAMADA("Llamada"),
    SEGUIMIENTO("Seguimiento"),
    RECEPCION_RESPUESTA("Recepción de respuesta"),
    CONFIRMACION("Confirmación")
}

/** Fuentes de información para detectar oportunidades (punto 3.4). */
enum class FuenteInformacion(val etiqueta: String) {
    REDES_SOCIALES("Redes sociales"),
    SITIO_WEB("Sitio web"),
    CORREO("Correo electrónico"),
    CONTACTO_DIRECTO("Contacto directo"),
    REFERENCIA("Referencia"),
    COMUNICACION_INTERNA("Comunicación interna")
}

/** Transiciones de estado permitidas según el rol (regla de negocio). */
object ReglasEstado {
    fun transicionesPermitidas(estado: EstadoOportunidad, rol: Rol): List<EstadoOportunidad> {
        if (estado.esFinal) return emptyList()
        return EstadoOportunidad.values().filter { destino ->
            !destino.esFinal && destino != estado ||
                    (rol == Rol.SUPERVISOR && destino.esFinal)
        }
    }
}
