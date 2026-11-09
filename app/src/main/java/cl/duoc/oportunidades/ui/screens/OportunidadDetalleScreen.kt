package cl.duoc.oportunidades.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.calendar.CalendarUtils
import cl.duoc.oportunidades.data.model.*
import cl.duoc.oportunidades.ui.navigation.Rutas
import cl.duoc.oportunidades.ui.theme.*
import cl.duoc.oportunidades.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OportunidadDetalleScreen(
    nav: NavHostController,
    id: Long,
    vm: AppViewModel = viewModel()
) {
    val o by remember(id) { vm.oportunidad(id) }.collectAsState()
    val organizaciones by vm.organizaciones.collectAsState()
    val seguimientos by remember(id) { vm.seguimientos(id) }.collectAsState()
    val guardado by vm.guardado.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var estadoAbierto by remember { mutableStateOf(false) }
    var mostrarResultado by remember { mutableStateOf(false) }

    AutoLimpiarBanner(guardado.exito, guardado.error) { vm.limpiarGuardado() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (o?.estado?.esFinal == false) {
                        IconButton(onClick = { nav.navigate(Rutas.form(id)) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val op = o
        if (op == null) {
            Cargando()
        } else {
            Column(
                Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BannerResultado(guardado.exito, guardado.error) { vm.limpiarGuardado() }

                Text(op.nombre, style = MaterialTheme.typography.headlineMedium, color = TextoPrincipal)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipEstado(op.estado)
                    ChipPrioridad(op.prioridad)
                }

                // ----- Cambio de estado (transiciones según rol) -----
                if (!op.estado.esFinal) {
                    val permitidos = ReglasEstado.transicionesPermitidas(op.estado,
                        vm.session.rol ?: Rol.EJECUTIVO)
                    Box {
                        OutlinedButton(onClick = { estadoAbierto = true },
                            modifier = Modifier.fillMaxWidth()) {
                            Text("Cambiar estado (${op.estado.etiqueta})")
                        }
                        DropdownMenu(expanded = estadoAbierto,
                            onDismissRequest = { estadoAbierto = false }) {
                            permitidos.forEach { e ->
                                DropdownMenuItem(
                                    text = { Text(e.etiqueta) },
                                    onClick = {
                                        vm.cambiarEstado(id, e)
                                        estadoAbierto = false
                                    }
                                )
                            }
                        }
                    }
                    if (permitidos.none { it.esFinal } && vm.session.rol == Rol.EJECUTIVO) {
                        Text("Solo el supervisor puede confirmar o descartar definitivamente.",
                            style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    }
                }

                // ----- Información del evento -----
                TarjetaInfo("Evento") {
                    FilaInfo("Tipo de oportunidad", op.tipo.etiqueta)
                    FilaInfo("Evento", op.eventoNombre)
                    FilaInfo("Tipo de evento", op.eventoTipo)
                    FilaInfo("Fechas", "${AppViewModel.longAFecha(op.fechaInicio)} al " +
                            AppViewModel.longAFecha(op.fechaTermino))
                    FilaInfo("Horario", op.horario)
                    FilaInfo("Ubicación", "${op.direccion}, ${op.comuna}, ${op.region}")
                    FilaInfo("Organización",
                        organizaciones.find { it.id == op.organizacionId }?.nombre ?: "-")
                    FilaInfo("Fuente", op.fuente.etiqueta)
                    FilaInfo("Detectada el", AppViewModel.longAFecha(op.fechaDeteccion))
                }

                TarjetaInfo("Público y potencial") {
                    FilaInfo("Público esperado", op.publicoEsperado)
                    FilaInfo("Público objetivo", op.publicoObjetivo)
                    FilaInfo("Asistentes estimados", op.cantidadAsistentesEstimada.toString())
                    FilaInfo("Potencial comercial", "${op.potencialComercial}/5")
                    FilaInfo("Prioridad", op.prioridad.etiqueta)
                }

                TarjetaInfo("Participación") {
                    FilaInfo("Condiciones", op.condiciones)
                    FilaInfo("Requisitos", op.requisitos)
                    FilaInfo("Costos referenciales", "$" + "%,.0f".format(op.costosReferenciales))
                    FilaInfo("Espacios disponibles", op.espaciosDisponibles)
                    FilaInfo("Propuesta enviada", if (op.propuestaEnviada) "Sí" else "No")
                }

                TarjetaInfo("Gestión") {
                    FilaInfo("Estado", op.estado.etiqueta)
                    FilaInfo("Gestiones registradas", seguimientos.size.toString())
                    FilaInfo("Última actualización",
                        AppViewModel.longAFecha(op.fechaUltimaActualizacion))
                }

                // ----- Resultado de activación -----
                if (op.resEstadoFinal != null) {
                    TarjetaInfo("Resultado de la activación") {
                        FilaInfo("Estado final", op.resEstadoFinal ?: "")
                        FilaInfo("Asistentes", (op.resAsistentes ?: 0).toString())
                        FilaInfo("Contactos generados", (op.resContactosGenerados ?: 0).toString())
                        FilaInfo("Personas interesadas", (op.resPersonasInteresadas ?: 0).toString())
                        FilaInfo("Inscripciones", (op.resInscripciones ?: 0).toString())
                        FilaInfo("Evaluación", "${op.resEvaluacion ?: 0}/5")
                        op.resObservaciones?.let { FilaInfo("Observaciones", it) }
                        op.resAprendizajes?.let { FilaInfo("Aprendizajes", it) }
                    }
                } else if (op.estado == EstadoOportunidad.PARTICIPACION_CONFIRMADA ||
                           op.estado == EstadoOportunidad.ACTIVACION_REALIZADA) {
                    Button(onClick = { mostrarResultado = true },
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.FactCheck, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Registrar resultado de activación")
                    }
                }

                // ----- Acciones rápidas -----
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            CalendarUtils.agregarEventoCalendario(
                                context, op.eventoNombre,
                                "${op.direccion}, ${op.comuna}",
                                op.fechaInicio, op.fechaTermino, op.descripcion)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Calendario")
                    }
                    OutlinedButton(
                        onClick = { nav.navigate(Rutas.seguimiento(id)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Seguimiento (${seguimientos.size})")
                    }
                }
                OutlinedButton(
                    onClick = { nav.navigate(Rutas.evidencias(id)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Evidencias fotográficas")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (mostrarResultado) {
        DialogoResultado(
            alGuardar = { datos ->
                vm.registrarResultado(id, datos)
                mostrarResultado = false
            },
            alCancelar = { mostrarResultado = false }
        )
    }
}

@Composable
fun TarjetaInfo(titulo: String, contenido: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Superficie),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = Primario)
            contenido()
        }
    }
}

@Composable
fun FilaInfo(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
        Text(valor, style = MaterialTheme.typography.bodyLarge, color = TextoPrincipal)
    }
}

/** Diálogo para registrar el resultado ficticio de una activación. */
@Composable
fun DialogoResultado(alGuardar: (Map<String, String>) -> Unit, alCancelar: () -> Unit) {
    var asistentes by remember { mutableStateOf("") }
    var contactos by remember { mutableStateOf("") }
    var interesados by remember { mutableStateOf("") }
    var inscripciones by remember { mutableStateOf("") }
    var evaluacion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var aprendizajes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Resultado de la activación") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CampoTexto(asistentes, { asistentes = it.filter { c -> c.isDigit() } },
                    "Asistentes (estimado)", obligatorio = true)
                CampoTexto(contactos, { contactos = it.filter { c -> c.isDigit() } },
                    "Contactos generados", obligatorio = true)
                CampoTexto(interesados, { interesados = it.filter { c -> c.isDigit() } },
                    "Personas interesadas")
                CampoTexto(inscripciones, { inscripciones = it.filter { c -> c.isDigit() } },
                    "Inscripciones")
                CampoTexto(evaluacion, { evaluacion = it.filter { c -> c.isDigit() } },
                    "Evaluación general (1-5)")
                CampoTexto(observaciones, { observaciones = it }, "Observaciones del equipo")
                CampoTexto(aprendizajes, { aprendizajes = it }, "Aprendizajes")
                error?.let { Text(it, color = ErrorColor) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (asistentes.isBlank() || contactos.isBlank()) {
                    error = "Asistentes y contactos generados son obligatorios"
                } else {
                    alGuardar(mapOf(
                        "asistentes" to asistentes, "contactos" to contactos,
                        "interesados" to interesados, "inscripciones" to inscripciones,
                        "evaluacion" to evaluacion, "observaciones" to observaciones,
                        "aprendizajes" to aprendizajes
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}
