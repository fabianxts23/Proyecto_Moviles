package cl.duoc.oportunidades.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.data.local.SeguimientoEntity
import cl.duoc.oportunidades.data.model.*
import cl.duoc.oportunidades.ui.theme.*
import cl.duoc.oportunidades.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoScreen(
    nav: NavHostController,
    id: Long,
    vm: AppViewModel = viewModel()
) {
    val oportunidad by remember(id) { vm.oportunidad(id) }.collectAsState()
    val seguimientos by remember(id) { vm.seguimientos(id) }.collectAsState()
    val guardado by vm.guardado.collectAsState()
    AutoLimpiarBanner(guardado.exito, guardado.error) { vm.limpiarGuardado() }

    var tipo by remember { mutableStateOf(TipoAccion.PRIMER_CONTACTO) }
    var resultado by remember { mutableStateOf("") }
    var proximaAccion by remember { mutableStateOf("") }
    var fechaProxima by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val fmt = remember { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BannerResultado(guardado.exito, guardado.error) { vm.limpiarGuardado() }
            oportunidad?.let { Text(it.nombre, style = MaterialTheme.typography.titleLarge,
                color = Primario) }

            // ----- Formulario de nueva gestión -----
            Card(colors = CardDefaults.cardColors(containerColor = Superficie),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nueva gestión", style = MaterialTheme.typography.titleMedium)
                    SelectorEnum("Tipo de acción", TipoAccion.values().toList(),
                        tipo.etiqueta, { tipo = it as TipoAccion })
                    CampoTexto(resultado, { resultado = it; error = null }, "Resultado",
                        error = error, obligatorio = true, unico = true)
                    CampoTexto(proximaAccion, { proximaAccion = it }, "Próxima acción",
                        obligatorio = true)
                    CampoTexto(fechaProxima, { fechaProxima = it }, "Fecha próximo seguimiento (yyyy-MM-dd)",
                        obligatorio = true)
                    Button(onClick = {
                        if (resultado.isBlank() || proximaAccion.isBlank() ||
                            fechaProxima.isBlank()) {
                            error = "Resultado, próxima acción y fecha son obligatorios"
                            return@Button
                        }
                        val errFecha = cl.duoc.oportunidades.domain.validation.Validators
                            .fechaValida(fechaProxima)
                        if (errFecha != null) { error = errFecha; return@Button }
                        val usuarioId = vm.session.usuarioActual?.id
                        if (usuarioId == null) {
                            error = "Sesión no válida. Vuelva a iniciar sesión."
                            return@Button
                        }
                        vm.registrarSeguimiento(SeguimientoEntity(
                            oportunidadId = id,
                            usuarioId = usuarioId,
                            tipoAccion = tipo,
                            fechaHora = System.currentTimeMillis(),
                            resultado = resultado.trim(),
                            proximaAccion = proximaAccion.trim(),
                            fechaProximo = AppViewModel.fechaALong(fechaProxima)
                        ))
                        resultado = ""; proximaAccion = ""; fechaProxima = ""
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Registrar gestión")
                    }
                }
            }

            // ----- Historial (nunca se borra) -----
            Text("Historial de gestiones (${seguimientos.size})",
                style = MaterialTheme.typography.titleLarge)
            if (seguimientos.isEmpty()) {
                EstadoVacio(Icons.Default.History, "Aún no hay gestiones registradas")
            } else {
                seguimientos.forEach { s ->
                    Card(colors = CardDefaults.cardColors(containerColor = Superficie),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(s.tipoAccion.etiqueta, style = MaterialTheme.typography.titleMedium,
                                color = Primario)
                            Text("Fecha: ${fmt.format(Date(s.fechaHora))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSecundario)
                            Text(s.resultado, style = MaterialTheme.typography.bodyLarge)
                            Text("Próxima acción: ${s.proximaAccion} — " +
                                    AppViewModel.longAFecha(s.fechaProximo),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (s.fechaProximo < System.currentTimeMillis())
                                    ErrorColor else Info)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
