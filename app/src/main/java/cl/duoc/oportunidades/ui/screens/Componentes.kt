package cl.duoc.oportunidades.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import cl.duoc.oportunidades.data.local.OportunidadEntity
import cl.duoc.oportunidades.data.model.*
import cl.duoc.oportunidades.ui.theme.*

/** Campo de texto con supportingText de error e ícono (checklist punto 4). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String? = null,
    obligatorio: Boolean = false,
    unico: Boolean = false,
    esClave: Boolean = false,
    placeholder: String = ""
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(if (obligatorio) "$etiqueta *" else etiqueta) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        isError = error != null,
        singleLine = !unico,
        visualTransformation = if (esClave && !visible)
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            when {
                esClave -> IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null)
                }
                error != null -> Icon(Icons.Default.Error, contentDescription = null,
                    tint = ErrorColor)
                else -> null
            }
        },
        supportingText = { error?.let { Text(it, color = ErrorColor) } },
        modifier = Modifier.fillMaxWidth()
    )
}

/** Chip de estado con color semántico. */
@Composable
fun ChipEstado(estado: EstadoOportunidad) {
    val color = colorEstado(estado)
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            estado.etiqueta,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Chip de prioridad con color semántico. */
@Composable
fun ChipPrioridad(prioridad: Prioridad) {
    val color = colorPrioridad(prioridad)
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            prioridad.etiqueta,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Tarjeta resumen de oportunidad para los listados. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaOportunidad(
    o: OportunidadEntity,
    subtitulo: String,
    alClick: () -> Unit
) {
    Card(
        onClick = alClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(o.nombre, style = MaterialTheme.typography.titleMedium,
                color = TextoPrincipal)
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChipEstado(o.estado)
                ChipPrioridad(o.prioridad)
            }
        }
    }
}

/** Estado vacío reutilizable. */
@Composable
fun EstadoVacio(icono: androidx.compose.ui.graphics.vector.ImageVector, mensaje: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icono, contentDescription = null, tint = TextoSecundario,
            modifier = Modifier.size(48.dp))
        Text(mensaje, style = MaterialTheme.typography.bodyLarge,
            color = TextoSecundario)
    }
}

/** Indicador de carga. */
@Composable
fun Cargando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Primario)
    }
}

/** Banner de éxito/error que se oculta automáticamente (animado). */
@Composable
fun BannerResultado(exito: String?, error: String?, alDescartar: () -> Unit) {
    val visible = exito != null || error != null
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        Surface(
            color = if (exito != null) ExitoFondo else ErrorFondo,
            contentColor = if (exito != null) Exito else ErrorColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(if (exito != null) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null)
                Text(exito ?: error ?: "", style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = alDescartar) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        }
    }
}

/** Limpia automáticamente el banner de resultado luego de unos segundos. */
@Composable
fun AutoLimpiarBanner(exito: String?, error: String?, limpiar: () -> Unit) {
    LaunchedEffect(exito, error) {
        if (exito != null || error != null) {
            delay(3000)
            limpiar()
        }
    }
}
