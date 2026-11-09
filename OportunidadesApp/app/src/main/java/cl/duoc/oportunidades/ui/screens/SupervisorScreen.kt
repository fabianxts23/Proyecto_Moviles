package cl.duoc.oportunidades.ui.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.data.model.Prioridad
import cl.duoc.oportunidades.data.model.Rol
import cl.duoc.oportunidades.ui.navigation.Rutas
import cl.duoc.oportunidades.ui.theme.*
import cl.duoc.oportunidades.ui.viewmodel.AppViewModel

/**
 * Panel del supervisor: consulta global del equipo, asignación de
 * responsables, cambio de prioridad y validación/descarte definitivo.
 * Solo accesible para el rol SUPERVISOR.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorScreen(nav: NavHostController, vm: AppViewModel = viewModel()) {
    val oportunidades by vm.oportunidades.collectAsState()
    val guardado by vm.guardado.collectAsState()

    var ejecutivosLista by remember { mutableStateOf<List<cl.duoc.oportunidades.data.local.UsuarioEntity>>(emptyList()) }
    LaunchedEffect(Unit) {
        ejecutivosLista = vm.repo.ejecutivos()
    }

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
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                BannerResultado(guardado.exito, guardado.error) { vm.limpiarGuardado() }
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
                        Text("Responsable: " + (ejecutivosLista.find {
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
                                ejecutivosLista.forEach { e ->
                                    DropdownMenuItem(
                                        text = { Text(e.nombre) },
                                        onClick = {
                                            vm.asignar(o.id, e.id)
                                            asignarAbierto = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Cambiar prioridad
                            Box(Modifier.weight(1f)) {
                                OutlinedButton(onClick = { prioridadAbierta = true },
                                    modifier = Modifier.fillMaxWidth()) {
                                    Text("Prioridad")
                                }
                                DropdownMenu(expanded = prioridadAbierta,
                                    onDismissRequest = { prioridadAbierta = false }) {
                                    Prioridad.values().forEach { p ->
                                        DropdownMenuItem(text = { Text(p.etiqueta) },
                                            onClick = {
                                                vm.cambiarPrioridad(o.id, p)
                                                prioridadAbierta = false
                                            })
                                    }
                                }
                            }
                            // Validar o descartar (solo supervisor)
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

            item { Spacer(Modifier.height(24.dp)) }
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
