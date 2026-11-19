package cl.duoc.oportunidades.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import cl.duoc.oportunidades.domain.validation.FormularioValidator
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.calendar.CalendarUtils
import cl.duoc.oportunidades.data.model.*
import cl.duoc.oportunidades.ui.navigation.Rutas
import cl.duoc.oportunidades.ui.theme.*

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

    // Nombre del responsable actual (se actualiza si el supervisor reasigna)
    var responsable by remember { mutableStateOf("-") }
    LaunchedEffect(o?.responsableId) {
        o?.let { responsable = vm.repo.usuario(it.responsableId)?.nombre ?: "-" }
    }

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
            Column(Modifier.padding(padding).fillMaxSize()) {
              // Banner fijo: siempre visible aunque la pantalla esté desplazada
              Box(Modifier.padding(horizontal = 16.dp)) {
                BannerResultado(guardado.exito, guardado.error) { vm.limpiarGuardado() }
              }
              Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
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
                    if (permitidos.none { it.esFinal }) {
                        Text("Los estados finales (participación confirmada, oportunidad descartada, " +
                                "activación realizada o cancelada) solo los puede fijar el supervisor.",
                            style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    }
                } else {
                    Surface(color = AdvertenciaFondo, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Oportunidad cerrada (${op.estado.etiqueta}): no admite más cambios de estado.",
                            style = MaterialTheme.typography.bodyMedium, color = TextoPrincipal,
                            modifier = Modifier.padding(12.dp))
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
                    FilaInfo("Público esperado (personas)", op.cantidadAsistentesEstimada.toString())
                    FilaInfo("Perfil del público esperado", op.publicoEsperado)
                    FilaInfo("Público objetivo", op.publicoObjetivo)
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
                    FilaInfo("Responsable", responsable)
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
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Resultado de la activación") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CampoTexto(asistentes, { asistentes = it.filter { c -> c.isDigit() } },
                    "Asistentes (estimado)", error = errores["asistentes"], obligatorio = true)
                CampoTexto(contactos, { contactos = it.filter { c -> c.isDigit() } },
                    "Contactos generados", error = errores["contactos"], obligatorio = true)
                CampoTexto(interesados, { interesados = it.filter { c -> c.isDigit() } },
                    "Personas interesadas", error = errores["interesados"])
                CampoTexto(inscripciones, { inscripciones = it.filter { c -> c.isDigit() } },
                    "Inscripciones", error = errores["inscripciones"])
                CampoTexto(evaluacion, { evaluacion = it.filter { c -> c.isDigit() } },
                    "Evaluación general (1-5)", error = errores["evaluacion"])
                CampoTexto(observaciones, { observaciones = it }, "Observaciones del equipo",
                    error = errores["observaciones"])
                CampoTexto(aprendizajes, { aprendizajes = it }, "Aprendizajes",
                    error = errores["aprendizajes"])
                FormularioValidator.resumen(errores)?.let { Text(it, color = ErrorColor) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val datos = mapOf(
                    "asistentes" to asistentes, "contactos" to contactos,
                    "interesados" to interesados, "inscripciones" to inscripciones,
                    "evaluacion" to evaluacion, "observaciones" to observaciones,
                    "aprendizajes" to aprendizajes
                )
                errores = FormularioValidator.resultado(datos)
                if (errores.isEmpty()) alGuardar(datos)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}
