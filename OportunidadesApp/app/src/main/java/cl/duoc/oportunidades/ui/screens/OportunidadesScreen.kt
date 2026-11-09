package cl.duoc.oportunidades.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import cl.duoc.oportunidades.data.model.*
import cl.duoc.oportunidades.ui.navigation.Rutas
import cl.duoc.oportunidades.ui.theme.*
import cl.duoc.oportunidades.ui.viewmodel.AppViewModel
import cl.duoc.oportunidades.ui.viewmodel.Filtros

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OportunidadesScreen(nav: NavHostController, vm: AppViewModel = viewModel()) {
    val lista by vm.oportunidades.collectAsState()
    val filtros by vm.filtros.collectAsState()

    // Animación 1: panel de filtros con AnimatedVisibility
    var mostrarFiltros by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oportunidades") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate(Rutas.form(null)) },
                containerColor = Primario,
                contentColor = Superficie
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva oportunidad")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Búsqueda por texto
            OutlinedTextField(
                value = filtros.texto,
                onValueChange = { vm.actualizarFiltros(filtros.copy(texto = it)) },
                placeholder = { Text("Buscar por nombre, evento o comuna") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Icon(
                            if (mostrarFiltros) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                            contentDescription = "Filtros",
                            tint = if (filtros.activos) Primario else TextoSecundario
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Panel de filtros animado
            AnimatedVisibility(
                visible = mostrarFiltros,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(Modifier.fillMaxWidth().padding(top = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Superficie)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Filtrar por estado", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            EstadoOportunidad.values().forEach { estado ->
                                FilterChip(
                                    selected = estado in filtros.estados,
                                    onClick = {
                                        val nuevo = if (estado in filtros.estados)
                                            filtros.estados - estado else filtros.estados + estado
                                        vm.actualizarFiltros(filtros.copy(estados = nuevo))
                                    },
                                    label = { Text(estado.etiqueta, style = MaterialTheme.typography.labelMedium) }
                                )
                            }
                        }
                        Text("Filtrar por prioridad", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Prioridad.values().forEach { p ->
                                FilterChip(
                                    selected = p in filtros.prioridades,
                                    onClick = {
                                        val nuevo = if (p in filtros.prioridades)
                                            filtros.prioridades - p else filtros.prioridades + p
                                        vm.actualizarFiltros(filtros.copy(prioridades = nuevo))
                                    },
                                    label = { Text(p.etiqueta) }
                                )
                            }
                        }
                        Text("Filtrar por tipo", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TipoOportunidad.values().forEach { t ->
                                FilterChip(
                                    selected = t in filtros.tipos,
                                    onClick = {
                                        val nuevo = if (t in filtros.tipos)
                                            filtros.tipos - t else filtros.tipos + t
                                        vm.actualizarFiltros(filtros.copy(tipos = nuevo))
                                    },
                                    label = { Text(t.etiqueta, style = MaterialTheme.typography.labelMedium) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = filtros.comuna,
                                onValueChange = { vm.actualizarFiltros(filtros.copy(comuna = it)) },
                                label = { Text("Comuna") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = filtros.region,
                                onValueChange = { vm.actualizarFiltros(filtros.copy(region = it)) },
                                label = { Text("Región") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        TextButton(onClick = { vm.limpiarFiltros() }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Limpiar filtros")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (lista.isEmpty()) {
                EstadoVacio(Icons.Default.Inbox, "No hay oportunidades con estos filtros")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(lista, key = { it.id }) { o ->
                        TarjetaOportunidad(
                            o = o,
                            subtitulo = "${o.tipo.etiqueta} · ${o.comuna}, ${o.region} · " +
                                    AppViewModel.longAFecha(o.fechaInicio),
                            alClick = { nav.navigate(Rutas.detalle(o.id)) }
                        )
                    }
                }
            }
        }
    }
}
