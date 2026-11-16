package cl.duoc.oportunidades.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.data.model.Prioridad
import cl.duoc.oportunidades.data.model.Rol
import cl.duoc.oportunidades.ui.navigation.Rutas
import cl.duoc.oportunidades.ui.theme.*

/**
 * Panel del supervisor: consulta global del equipo, asignación de
 * responsables, cambio de prioridad y validación/descarte definitivo.
 * Solo accesible para el rol SUPERVISOR.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorScreen(
    nav: NavHostController,
    vm: AppViewModel = viewModel(),
    usuarioVm: UsuarioViewModel = viewModel()
) {
    val estadoUsuarios by usuarioVm.uiState.collectAsStateWithLifecycle()
    val oportunidades by vm.oportunidades.collectAsState()
    val guardado by vm.guardado.collectAsState()

    // Todos los usuarios activos: sirven para mostrar el responsable actual; los ejecutivos
    // son los candidatos para asignar o reasignar.
    var usuariosLista by remember { mutableStateOf<List<cl.duoc.oportunidades.data.local.UsuarioEntity>>(emptyList()) }
    LaunchedEffect(Unit) {
        usuariosLista = vm.repo.usuarios()
    }
    val ejecutivosLista = usuariosLista.filter { it.rol == Rol.EJECUTIVO }

    AutoLimpiarBanner(guardado.exito, guardado.error) { vm.limpiarGuardado() }

    if (vm.session.rol != Rol.SUPERVISOR) {
        Column(Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center) {
            Text("Acceso restringido al supervisor", color = ErrorColor,
                style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    val activas = oportunidades.filter { !it.estado.esFinal }
    val cerradas = oportunidades.filter { it.estado.esFinal }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel supervisor") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
        // Banner fijo: se ve el resultado de la acción aunque la lista esté desplazada
        Box(Modifier.padding(horizontal = 16.dp)) {
            BannerResultado(guardado.exito, guardado.error) { vm.limpiarGuardado() }
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                // Indicadores generales
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TarjetaIndicador("Activas", activas.size, Info, Modifier.weight(1f))
                    TarjetaIndicador("Cerradas", cerradas.size, Exito, Modifier.weight(1f))
                    TarjetaIndicador("Críticas/Alta",
                        activas.count { it.prioridad == Prioridad.CRITICA ||
                                it.prioridad == Prioridad.ALTA }, Advertencia, Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                Text("Gestiones del equipo", style = MaterialTheme.typography.titleLarge)
            }

            if (activas.isEmpty()) {
                item { EstadoVacio(Icons.Default.TaskAlt, "No hay gestiones activas") }
            }

            items(activas, key = { it.id }) { o ->
                var asignarAbierto by remember { mutableStateOf(false) }
                var prioridadAbierta by remember { mutableStateOf(false) }

                Card(colors = CardDefaults.cardColors(containerColor = Superficie),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(o.nombre, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChipEstado(o.estado)
                            ChipPrioridad(o.prioridad)
                        }
                        Text("Responsable: " + (usuariosLista.find {
                            it.id == o.responsableId }?.nombre ?: "Sin asignar"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoSecundario)

                        // Asignar / reasignar ejecutivo
                        Box {
                            OutlinedButton(onClick = { asignarAbierto = true },
                                modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Asignar / reasignar ejecutivo")
                            }
                            DropdownMenu(expanded = asignarAbierto,
                                onDismissRequest = { asignarAbierto = false }) {
                                if (ejecutivosLista.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No hay ejecutivos disponibles") },
                                        onClick = { asignarAbierto = false }
                                    )
                                }
                                ejecutivosLista.forEach { e ->
                                    DropdownMenuItem(
                                        text = { Text(if (e.id == o.responsableId)
                                            e.nombre + " (actual)" else e.nombre) },
                                        enabled = e.id != o.responsableId,
                                        onClick = {
                                            vm.asignar(o.id, e)
                                            asignarAbierto = false
                                        }
                                    )
                                }
                            }
                        }

                        // Cambiar prioridad (baja, media, alta o crítica)
                        Box {
                            OutlinedButton(onClick = { prioridadAbierta = true },
                                modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Flag, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Cambiar prioridad (${o.prioridad.etiqueta})")
                            }
                            DropdownMenu(expanded = prioridadAbierta,
                                onDismissRequest = { prioridadAbierta = false }) {
                                Prioridad.values().forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(if (p == o.prioridad)
                                            p.etiqueta + " (actual)" else p.etiqueta) },
                                        enabled = p != o.prioridad,
                                        onClick = {
                                            vm.cambiarPrioridad(o.id, p)
                                            prioridadAbierta = false
                                        })
                                }
                            }
                        }
                        // Confirmar o descartar definitivamente (solo supervisor)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.validarOdescartar(o.id, confirmar = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Exito),
                                modifier = Modifier.weight(1f)) {
                                Text("Confirmar")
                            }
                            Button(onClick = { vm.validarOdescartar(o.id, confirmar = false) },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                                modifier = Modifier.weight(1f)) {
                                Text("Descartar")
                            }
                        }
                    }
                }
            }

            item {
                SeccionUsuariosServidor(estadoUsuarios) { usuarioVm.cargarUsuarios() }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
        }
    }
}


@Composable
fun TarjetaIndicador(titulo: String, valor: Int, color: androidx.compose.ui.graphics.Color,
                     modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(valor.toString(), style = MaterialTheme.typography.headlineMedium, color = color)
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = TextoPrincipal)
        }
    }

}

/** Usuarios que entrega el backend (GET /v1/api/usuarios), con estados de carga, error y reintento. */
@Composable
fun SeccionUsuariosServidor(estado: UsuarioUiState, onReintentar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Usuarios del servidor", style = MaterialTheme.typography.titleLarge)
        when (estado) {
            is UsuarioUiState.Cargando -> Box(
                Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is UsuarioUiState.Error -> Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(estado.mensaje, color = ErrorColor)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onReintentar) { Text("Reintentar") }
            }

            is UsuarioUiState.Exito -> {
                if (estado.usuarios.isEmpty()) {
                    Text("No hay usuarios registrados en el servidor.", color = TextoSecundario)
                }
                estado.usuarios.forEach { u ->
                    Card(colors = CardDefaults.cardColors(containerColor = Superficie),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(u.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(u.correo, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                            Text("${u.rol} · ${u.zona ?: "-"}" + if (!u.cuentaActiva) " · inactiva" else "",
                                style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                        }
                    }
                }
            }
        }
    }
}
