package cl.duoc.oportunidades.ui

import androidx.compose.foundation.layout.*
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
import cl.duoc.oportunidades.domain.validation.FormularioValidator
import cl.duoc.oportunidades.domain.validation.Validators
import cl.duoc.oportunidades.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private const val DIA_MS = 24L * 60 * 60 * 1000

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

    val fmt = remember { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()) }
    val fmtFecha = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val fmtHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Formulario de nueva gestión (la fecha y hora parten con el momento actual)
    var tipo by remember { mutableStateOf(TipoAccion.PRIMER_CONTACTO) }
    var fechaAccion by remember { mutableStateOf(fmtFecha.format(Date())) }
    var horaAccion by remember { mutableStateOf(fmtHora.format(Date())) }
    var resultado by remember { mutableStateOf("") }
    var proximaAccion by remember { mutableStateOf("") }
    var fechaProxima by remember { mutableStateOf("") }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

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
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Banner fijo: siempre visible aunque la pantalla esté desplazada
            Box(Modifier.padding(horizontal = 16.dp)) {
                BannerResultado(guardado.exito, guardado.error) { vm.limpiarGuardado() }
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                oportunidad?.let { Text(it.nombre, style = MaterialTheme.typography.titleLarge,
                    color = Primario) }

                // ----- Formulario de nueva gestión -----
                Card(colors = CardDefaults.cardColors(containerColor = Superficie),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nueva gestión", style = MaterialTheme.typography.titleMedium)
                        SelectorChips("Tipo de acción", TipoAccion.values().toList(), tipo,
                            { it.etiqueta }, { tipo = it })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(2f)) {
                                CampoTexto(fechaAccion, { fechaAccion = it }, "Fecha (yyyy-MM-dd)",
                                    error = errores["fechaAccion"], obligatorio = true)
                            }
                            Box(Modifier.weight(1f)) {
                                CampoTexto(horaAccion, { horaAccion = it }, "Hora",
                                    error = errores["horaAccion"], obligatorio = true,
                                    placeholder = "15:30")
                            }
                        }
                        CampoTexto(resultado, { resultado = it }, "Resultado",
                            error = errores["resultado"], obligatorio = true, unico = true)
                        CampoTexto(proximaAccion, { proximaAccion = it }, "Próxima acción",
                            error = errores["proximaAccion"], obligatorio = true)
                        CampoTexto(fechaProxima, { fechaProxima = it },
                            "Fecha próximo seguimiento (yyyy-MM-dd)",
                            error = errores["fechaProxima"], obligatorio = true)
                        Button(onClick = {
                            val e = FormularioValidator.seguimiento(mapOf(
                                "fechaAccion" to fechaAccion, "horaAccion" to horaAccion,
                                "resultado" to resultado, "proximaAccion" to proximaAccion,
                                "fechaProxima" to fechaProxima))
                            errores = e
                            if (e.isNotEmpty()) {
                                // Aviso visible arriba, además de los errores bajo cada campo
                                vm.mostrarErrorPublico(
                                    FormularioValidator.resumen(e) ?: "Revise los campos")
                                return@Button
                            }

                            val usuarioId = vm.session.usuarioActual?.id
                            if (usuarioId == null) {
                                vm.mostrarErrorPublico("Sesión no válida. Vuelva a iniciar sesión.")
                                return@Button
                            }
                            val momento = try {
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    .apply { isLenient = false }
                                    .parse("${fechaAccion.trim()} ${horaAccion.trim()}")?.time
                                    ?: System.currentTimeMillis()
                            } catch (ex: Exception) {
                                System.currentTimeMillis()
                            }
                            vm.registrarSeguimiento(SeguimientoEntity(
                                oportunidadId = id,
                                usuarioId = usuarioId,
                                tipoAccion = tipo,
                                fechaHora = momento,
                                resultado = resultado.trim(),
                                proximaAccion = proximaAccion.trim(),
                                // La próxima acción vence al terminar el día indicado
                                fechaProximo = AppViewModel.fechaALong(fechaProxima.trim()) + DIA_MS - 1
                            ))
                            resultado = ""; proximaAccion = ""; fechaProxima = ""
                            fechaAccion = fmtFecha.format(Date())
                            horaAccion = fmtHora.format(Date())
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Registrar gestión")
                        }
                    }
                }

                // ----- Historial (nunca se borra; el más reciente primero) -----
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
}
