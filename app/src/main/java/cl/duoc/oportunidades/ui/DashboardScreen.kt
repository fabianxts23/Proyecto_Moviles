package cl.duoc.oportunidades.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.data.model.Rol
import cl.duoc.oportunidades.data.local.OportunidadEntity
import cl.duoc.oportunidades.ui.navigation.Rutas
import cl.duoc.oportunidades.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    nav: NavHostController,
    vm: AppViewModel = viewModel(),
    auth: AuthViewModel = viewModel()
) {
    val oportunidades by vm.oportunidades.collectAsState()
    val alertas by vm.alertas.collectAsState()
    val usuario = vm.session.usuarioActual
    // El ejecutivo ve solo sus oportunidades; el supervisor, las del equipo.
    val visibles = if (usuario?.rol == Rol.SUPERVISOR) oportunidades
        else oportunidades.filter { it.responsableId == usuario?.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hola, ${usuario?.nombre ?: "usuario"}") },
                actions = {
                    IconButton(onClick = {
                        auth.logout()
                        nav.navigate(Rutas.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Crossfade(targetState = oportunidades.isEmpty(), label = "dash") { vacio ->
            if (vacio) {
                Cargando()
            } else {
                Column(
                    Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Resumen según rol
                    TarjetaResumen(
                        titulo = if (usuario?.rol == Rol.SUPERVISOR)
                            "Oportunidades del equipo" else "Mis oportunidades",
                        valor = visibles.size.toString(),
                        descripcion = "${visibles.count { it.estado.esFinal }} cerradas · " +
                                "${visibles.count { !it.estado.esFinal }} activas"
                    )

                    // Alertas y recordatorios: cada tipo con su ícono y color
                    val ahora = System.currentTimeMillis()
                    Text(if (alertas.total == 0) "Alertas y recordatorios"
                         else "Alertas y recordatorios (${alertas.total})",
                        style = MaterialTheme.typography.titleLarge)
                    if (alertas.total == 0) {
                        Card(colors = CardDefaults.cardColors(containerColor = ExitoFondo)) {
                            Text("No tienes gestiones pendientes",
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        // Eventos que inician dentro de los próximos 14 días
                        alertas.proximas.forEach { AlertaFila(it, "Evento próximo",
                            "Inicia " + cuandoInicia(it.fechaInicio, ahora),
                            Icons.Default.Event, Advertencia) { nav.navigate(Rutas.detalle(it.id)) } }
                        // La próxima acción de la última gestión ya venció
                        alertas.atrasadas.forEach { AlertaFila(it, "Gestión atrasada",
                            "La próxima acción de seguimiento ya venció",
                            Icons.Default.Warning, ErrorColor) { nav.navigate(Rutas.detalle(it.id)) } }
                        // Propuestas enviadas hace más de 7 días sin respuesta
                        alertas.propuestasSinRespuesta.forEach { AlertaFila(it, "Propuesta sin respuesta",
                            "Enviada " + haceDias(it.fechaPropuesta ?: it.fechaUltimaActualizacion, ahora),
                            Icons.Default.MailOutline, Info) { nav.navigate(Rutas.detalle(it.id)) } }
                        alertas.sinContacto.forEach { AlertaFila(it, "Sin contacto",
                            "Detectada " + haceDias(it.fechaDeteccion, ahora) + " y aún sin contactar",
                            Icons.Default.PhoneDisabled, Advertencia) { nav.navigate(Rutas.detalle(it.id)) } }
                        alertas.sinSeguimiento.forEach { AlertaFila(it, "Sin seguimiento",
                            "No tiene gestiones registradas",
                            Icons.Default.FollowTheSigns, Info) { nav.navigate(Rutas.detalle(it.id)) } }
                    }

                    // Accesos rápidos
                    Text("Accesos rápidos", style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BotonAcceso("Oportunidades", Icons.Default.List, Modifier.weight(1f)) {
                            nav.navigate(Rutas.OPORTUNIDADES)
                        }
                        BotonAcceso("Organizaciones", Icons.Default.Apartment, Modifier.weight(1f)) {
                            nav.navigate(Rutas.ORGANIZACIONES)
                        }
                    }
                    if (usuario?.rol == Rol.SUPERVISOR) {
                        BotonAcceso("Panel supervisor", Icons.Default.SupervisorAccount,
                            Modifier.fillMaxWidth()) { nav.navigate(Rutas.SUPERVISOR) }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaResumen(titulo: String, valor: String, descripcion: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Primario),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = Superficie)
            Text(valor, style = MaterialTheme.typography.headlineMedium, color = AmarilloDuoc)
            Text(descripcion, style = MaterialTheme.typography.bodyMedium,
                color = Superficie.copy(alpha = 0.85f))
        }
    }
}

private const val DIA_MS = 24L * 60 * 60 * 1000

/** "hoy", "mañana" o "en N días" según los días calendario que faltan. */
private fun cuandoInicia(inicio: Long, ahora: Long): String {
    val d = ((AppViewModel.inicioDelDia(inicio) - AppViewModel.inicioDelDia(ahora)) / DIA_MS).toInt()
    return when {
        d <= 0 -> "hoy"
        d == 1 -> "mañana"
        else -> "en $d días"
    }
}

/** "hoy", "ayer" o "hace N días" según los días calendario transcurridos. */
private fun haceDias(fecha: Long, ahora: Long): String {
    val d = ((AppViewModel.inicioDelDia(ahora) - AppViewModel.inicioDelDia(fecha)) / DIA_MS).toInt()
    return when {
        d <= 0 -> "hoy"
        d == 1 -> "ayer"
        else -> "hace $d días"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertaFila(
    o: OportunidadEntity,
    motivo: String,
    detalle: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    alClick: () -> Unit
) {
    Card(onClick = alClick, colors = CardDefaults.cardColors(containerColor = Superficie),
        modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = color)
            Column(Modifier.weight(1f)) {
                Text(motivo, style = MaterialTheme.typography.titleMedium, color = color)
                Text(o.nombre, style = MaterialTheme.typography.bodyMedium,
                    color = TextoPrincipal)
                Text(detalle, style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = TextoSecundario)
        }
    }
}

@Composable
fun BotonAcceso(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    alClick: () -> Unit
) {
    Button(onClick = alClick, modifier = modifier) {
        Icon(icono, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(texto)
    }
}
