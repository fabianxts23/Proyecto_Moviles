package cl.duoc.oportunidades.ui.theme

import androidx.compose.ui.graphics.Color
import cl.duoc.oportunidades.data.model.EstadoOportunidad
import cl.duoc.oportunidades.data.model.Prioridad

/** Paleta centralizada: ningún color hexadecimal en las pantallas. */

// Marca
val AzulDuoc = Color(0xFF003C71)
val AzulDuocClaro = Color(0xFF1565C0)
val AmarilloDuoc = Color(0xFFFFD100)

// Esquema Material 3
val Primario = AzulDuoc
val PrimarioContenedor = Color(0xFFD6E4F0)
val Secundario = Color(0xFF00695C)
val Fondo = Color(0xFFF7F9FC)
val Superficie = Color(0xFFFFFFFF)
val SuperficieVariante = Color(0xFFEEF1F6)
val TextoPrincipal = Color(0xFF1B1B1F)
val TextoSecundario = Color(0xFF5A5F6A)
val BordeSuave = Color(0xFFE0E4EA)

// Colores semánticos
val Exito = Color(0xFF2E7D32)
val ExitoFondo = Color(0xFFE6F2E6)
val Advertencia = Color(0xFFF9A825)
val AdvertenciaFondo = Color(0xFFFFF6DE)
val ErrorColor = Color(0xFFB3261E)
val ErrorFondo = Color(0xFFFCE8E6)
val Info = Color(0xFF1565C0)
val InfoFondo = Color(0xFFE3F0FB)

/** Color semántico por estado de oportunidad. */
fun colorEstado(estado: EstadoOportunidad) = when (estado) {
    EstadoOportunidad.PARTICIPACION_CONFIRMADA,
    EstadoOportunidad.ACTIVACION_REALIZADA -> Exito
    EstadoOportunidad.PROPUESTA_ENVIADA,
    EstadoOportunidad.EN_ESPERA_RESPUESTA -> Info
    EstadoOportunidad.INFORMACION_INCOMPLETA,
    EstadoOportunidad.REQUIERE_SEGUIMIENTO,
    EstadoOportunidad.CONTACTO_PENDIENTE -> Advertencia
    EstadoOportunidad.OPORTUNIDAD_DESCARTADA,
    EstadoOportunidad.ACTIVACION_CANCELADA -> ErrorColor
    else -> AzulDuoc
}

/** Color por prioridad. */
fun colorPrioridad(p: Prioridad) = when (p) {
    Prioridad.CRITICA -> ErrorColor
    Prioridad.ALTA -> Advertencia
    Prioridad.MEDIA -> Info
    Prioridad.BAJA -> TextoSecundario
}
