package cl.duoc.oportunidades.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.data.model.*
import cl.duoc.oportunidades.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OportunidadFormScreen(
    nav: NavHostController,
    id: Long?,
    vm: AppViewModel = viewModel()
) {
    val guardado by vm.guardado.collectAsState()
    val organizaciones by vm.organizaciones.collectAsState()

    // Si es edición, cargamos la oportunidad existente
    val existenteFlow = remember(id) { id?.let { vm.oportunidad(it) } }
    val existente = existenteFlow?.collectAsState()?.value

    // ----- Campos (se mantienen al fallar la validación) -----
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var eventoNombre by remember { mutableStateOf("") }
    var eventoTipo by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaTermino by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var publicoEsperado by remember { mutableStateOf("") }
    var publicoObjetivo by remember { mutableStateOf("") }
    var asistentes by remember { mutableStateOf("") }
    var condiciones by remember { mutableStateOf("") }
    var requisitos by remember { mutableStateOf("") }
    var costos by remember { mutableStateOf("") }
    var espacios by remember { mutableStateOf("") }
    var potencial by remember { mutableStateOf("") }

    // Tipo, fuente y organización parten sin seleccionar y son obligatorios
    var tipo by remember { mutableStateOf<TipoOportunidad?>(null) }
    var fuente by remember { mutableStateOf<FuenteInformacion?>(null) }
    var prioridad by remember { mutableStateOf(Prioridad.MEDIA) }
    var organizacionId by remember { mutableStateOf<Long?>(null) }

    var responsable by remember { mutableStateOf(vm.session.usuarioActual?.nombre ?: "-") }
    var cargado by remember { mutableStateOf(false) }

    // Cargar datos al editar (una sola vez, para no pisar lo que el usuario escribe)
    LaunchedEffect(existente) {
        val e = existente
        if (e != null && !cargado) {
            cargado = true
            nombre = e.nombre; descripcion = e.descripcion
            eventoNombre = e.eventoNombre; eventoTipo = e.eventoTipo
            fechaInicio = AppViewModel.longAFecha(e.fechaInicio)
            fechaTermino = AppViewModel.longAFecha(e.fechaTermino)
            horario = e.horario; direccion = e.direccion
            comuna = e.comuna; region = e.region
            publicoEsperado = e.publicoEsperado; publicoObjetivo = e.publicoObjetivo
            asistentes = e.cantidadAsistentesEstimada.toString()
            condiciones = e.condiciones; requisitos = e.requisitos
            // Costos en pesos sin decimales (evita "1.5E7" en montos grandes)
            costos = if (e.costosReferenciales % 1.0 == 0.0)
                e.costosReferenciales.toLong().toString() else e.costosReferenciales.toString()
            espacios = e.espaciosDisponibles
            potencial = e.potencialComercial.toString()
            tipo = e.tipo; fuente = e.fuente; prioridad = e.prioridad
            organizacionId = e.organizacionId
            responsable = vm.repo.usuario(e.responsableId)?.nombre ?: "-"
        }
    }

    // Navegar al guardar con éxito
    LaunchedEffect(guardado.exito) {
        guardado.exito?.let { nav.popBackStack() }
    }

    // El mensaje superior se oculta solo; los errores bajo cada campo se conservan
    AutoLimpiarBanner(null, guardado.error) { vm.limpiarMensaje() }
    val errores = guardado.errores

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "Nueva oportunidad" else "Editar oportunidad") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Banner fijo: siempre visible aunque el formulario esté desplazado
            Box(Modifier.padding(horizontal = 16.dp)) {
                BannerResultado(guardado.exito, guardado.error) { vm.limpiarMensaje() }
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SeccionFormulario("Oportunidad")
                CampoTexto(nombre, { nombre = it }, "Nombre de la oportunidad",
                    error = errores["nombre"], obligatorio = true)
                CampoTexto(descripcion, { descripcion = it }, "Descripción",
                    error = errores["descripcion"], obligatorio = true, unico = true)
                SelectorOpciones("Tipo de oportunidad", TipoOportunidad.values().toList(),
                    tipo, { it.etiqueta }, { tipo = it },
                    error = errores["tipo"], obligatorio = true)
                SelectorOpciones("Fuente de información", FuenteInformacion.values().toList(),
                    fuente, { it.etiqueta }, { fuente = it },
                    error = errores["fuente"], obligatorio = true)

                SeccionFormulario("Evento")
                CampoTexto(eventoNombre, { eventoNombre = it }, "Nombre del evento",
                    error = errores["eventoNombre"], obligatorio = true)
                CampoTexto(eventoTipo, { eventoTipo = it }, "Tipo de evento",
                    error = errores["eventoTipo"], obligatorio = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        CampoTexto(fechaInicio, { fechaInicio = it }, "Inicio (yyyy-MM-dd)",
                            error = errores["fechaInicio"], obligatorio = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CampoTexto(fechaTermino, { fechaTermino = it }, "Término (yyyy-MM-dd)",
                            error = errores["fechaTermino"], obligatorio = true)
                    }
                }
                CampoTexto(horario, { horario = it }, "Horario",
                    error = errores["horario"], obligatorio = true, placeholder = "10:00 - 18:00")
                CampoTexto(direccion, { direccion = it }, "Dirección o ubicación",
                    error = errores["direccion"], obligatorio = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        CampoTexto(comuna, { comuna = it }, "Comuna",
                            error = errores["comuna"], obligatorio = true)
                    }
                    Box(Modifier.weight(1f)) {
                        CampoTexto(region, { region = it }, "Región",
                            error = errores["region"], obligatorio = true)
                    }
                }

                SeccionFormulario("Organización responsable")
                SelectorOpciones("Organización", organizaciones,
                    organizaciones.find { it.id == organizacionId },
                    { it.nombre }, { organizacionId = it.id },
                    error = errores["organizacion"], obligatorio = true)

                SeccionFormulario("Público esperado")
                // Se permite escribir "-" para que la validación informe los valores negativos
                CampoTexto(asistentes, { asistentes = it.filter { ch -> ch.isDigit() || ch == '-' } },
                    "Público esperado (cantidad de personas)",
                    error = errores["asistentes"], obligatorio = true)
                CampoTexto(publicoEsperado, { publicoEsperado = it },
                    "Perfil del público esperado",
                    error = errores["publicoEsperado"], obligatorio = true)
                CampoTexto(publicoObjetivo, { publicoObjetivo = it }, "Público objetivo / afinidad",
                    error = errores["publicoObjetivo"], obligatorio = true)

                SeccionFormulario("Participación y costos")
                CampoTexto(condiciones, { condiciones = it }, "Condiciones de participación",
                    error = errores["condiciones"], obligatorio = true, unico = true)
                CampoTexto(requisitos, { requisitos = it }, "Requisitos",
                    error = errores["requisitos"], obligatorio = true, unico = true)
                CampoTexto(costos, { costos = it.filter { ch -> ch.isDigit() || ch == '-' } },
                    "Costos referenciales (CLP, use 0 si no aplica)",
                    error = errores["costos"], obligatorio = true)
                CampoTexto(espacios, { espacios = it }, "Espacios disponibles",
                    error = errores["espacios"], obligatorio = true)

                SeccionFormulario("Gestión")
                SelectorOpciones("Prioridad", Prioridad.values().toList(), prioridad,
                    { it.etiqueta }, { prioridad = it })
                CampoTexto(potencial, { potencial = it.filter { ch -> ch.isDigit() } },
                    "Potencial comercial (1-5)", error = errores["potencial"], obligatorio = true)
                Text("Estado: " + (existente?.estado ?: EstadoOportunidad.EVENTO_IDENTIFICADO).etiqueta,
                    style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                Text("Responsable: $responsable" +
                        if (id == null) " (queda asignada a usted al guardar)" else "",
                    style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val campos = mapOf(
                            "nombre" to nombre, "descripcion" to descripcion,
                            "eventoNombre" to eventoNombre, "eventoTipo" to eventoTipo,
                            "fechaInicio" to fechaInicio, "fechaTermino" to fechaTermino,
                            "horario" to horario, "direccion" to direccion,
                            "comuna" to comuna, "region" to region,
                            "publicoEsperado" to publicoEsperado,
                            "publicoObjetivo" to publicoObjetivo,
                            "asistentes" to asistentes, "condiciones" to condiciones,
                            "requisitos" to requisitos, "costos" to costos,
                            "espacios" to espacios, "potencial" to potencial
                        )
                        vm.guardarOportunidad(existente, campos, tipo, fuente,
                            prioridad, organizacionId)
                    },
                    // En edición se espera a que carguen los datos para no crear un duplicado
                    enabled = !guardado.guardando && (id == null || existente != null),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (guardado.guardando) CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), color = Superficie, strokeWidth = 2.dp)
                    else Text(if (id == null) "Crear oportunidad" else "Guardar cambios")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SeccionFormulario(titulo: String) {
    Spacer(Modifier.height(14.dp))
    Text(titulo, style = MaterialTheme.typography.titleLarge, color = Primario)
    Spacer(Modifier.height(4.dp))
    HorizontalDivider(color = BordeSuave)
    Spacer(Modifier.height(8.dp))
}
