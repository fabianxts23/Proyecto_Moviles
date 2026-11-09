package cl.duoc.oportunidades.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = Primario,
    onPrimary = Superficie,
    primaryContainer = PrimarioContenedor,
    onPrimaryContainer = TextoPrincipal,
    secondary = Secundario,
    onSecondary = Superficie,
    background = Fondo,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,
    outline = BordeSuave,
    error = ErrorColor,
    onError = Superficie
)

@Composable
fun OportunidadesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Tipografia,
        content = content
    )
}
