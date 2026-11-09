package cl.duoc.oportunidades.ui.screens

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
import cl.duoc.oportunidades.ui.viewmodel.AppViewModel

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

    var tipo by remember { mutableStateOf(TipoOportunidad.EVENTO_CORPORATIVO) }
    var fuente by remember { mutableStateOf(FuenteInformacion.SITIO_WEB) }
    var prioridad by remember { mutableStateOf(Prioridad.MEDIA) }
    var organizacionId by remember { mutableStateOf<Long?>(null) }
    var orgAbierta by remember { mutableStateOf(false) }

    // Cargar datos al editar
    LaunchedEffect(existente) {
        existente?.let { e ->
            nombre = e.nombre; descripcion = e.descripcion
            eventoNombre = e.eventoNombre; eventoTipo = e.eventoTipo
            fechaInicio = AppViewModel.longAFecha(e.fechaInicio)
            fechaTermino = AppViewModel.longAFecha(e.fechaTermino)
            horario = e.horario; direccion = e.direccion
            comuna = e.comuna; region = e.region
            publicoEsperado = e.publicoEsperado; publicoObjetivo = e.publicoObjetivo
            asistentes = e.cantidadAsistentesEstimada.toString()
            condiciones = e.condiciones; requisitos = e.requisitos
            costos = e.costosReferenciales.toString(); espacios = e.espaciosDisponibles
            potencial = e.potencialComercial.toString()
            tipo = e.tipo; fuente = e.fuente; prioridad = e.prioridad
            organizacionId = e.organizacionId
        }
    }

    // Navegar al guardar con éxito
    LaunchedEffect(guardado.exito) {
        guardado.exito?.let { nav.popBackStack() }
    }

    AutoLimpiarBanner(null, guardado.error) { vm.limpiarGuardado() }
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
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BannerResultado(guardado.exito, guardado.error) { vm.limpiarGuardado() }

            SeccionFormulario("Oportunidad")
            CampoTexto(nombre, { nombre = it }, "Nombre de la oportunidad",
                error = errores["nombre"], obligatorio = true)
            CampoTexto(descripcion, { descripcion = it }, "Descripción",
                error = errores["descripcion"], obligatorio = true, unico = true)
            SelectorEnum("Tipo de oportunidad", TipoOportunidad.values().toList(),
                tipo.etiqueta, { tipo = it as TipoOportunidad })
            SelectorEnum("Fuente de información", FuenteInformacion.values().toList(),
                fuente.etiqueta, { fuente = it as FuenteInformacion })

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
            Box {
                OutlinedButton(
                    onClick = { orgAbierta = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(organizaciones.find { it.id == organizacionId }?.nombre
                        ?: "Seleccionar organización")
                }
                DropdownMenu(expanded = orgAbierta, onDismissRequest = { orgAbierta = false }) {
                    organizaciones.forEach { org ->
                        DropdownMenuItem(
                            text = { Text(org.nombre) },
                            onClick = { organizacionId = org.id; orgAbierta = false }
                        )
                    }
                }
            }
            if (organizacionId == null && errores.containsKey("organizacion")) {
                Text(errores["organizacion"]!!, color = ErrorColor,
                    style = MaterialTheme.typography.bodyMedium)
            }

            SeccionFormulario("Público y evaluación")
            CampoTexto(publicoEsperado, { publicoEsperado = it }, "Público esperado",
                error = errores["publicoEsperado"], obligatorio = true)
            CampoTexto(publicoObjetivo, { publicoObjetivo = it }, "Público objetivo / afinidad",
                error = errores["publicoObjetivo"], obligatorio = true)
            CampoTexto(asistentes, { asistentes = it.filter { ch -> ch.isDigit() } },
                "Cantidad estimada de asistentes",
                error = errores["asistentes"], obligatorio = true)
            CampoTexto(potencial, { potencial = it.filter { ch -> ch.isDigit() } },
                "Potencial comercial (1-5)", error = errores["potencial"], obligatorio = true)
            SelectorEnum("Prioridad", Prioridad.values().toList(), prioridad.etiqueta,
                { prioridad = it as Prioridad })

            SeccionFormulario("Participación")
            CampoTexto(condiciones, { condiciones = it }, "Condiciones de participación",
                error = errores["condiciones"], obligatorio = true, unico = true)
            CampoTexto(requisitos, { requisitos = it }, "Requisitos",
                error = errores["requisitos"], obligatorio = true, unico = true)
            CampoTexto(costos, { costos = it.filter { ch -> ch.isDigit() || ch == '.' } },
                "Costos referenciales (CLP)", error = errores["costos"], obligatorio = true)
            CampoTexto(espacios, { espacios = it }, "Espacios disponibles",
                error = errores["espacios"], obligatorio = true)

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
                    if (organizacionId == null) {
                        vm.mostrarErrorPublico("Debe seleccionar una organización responsable")
                    } else {
                        vm.guardarOportunidad(existente, campos, tipo, fuente,
                            prioridad, organizacionId!!)
                    }
                },
                enabled = !guardado.guardando,
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

@Composable
fun SeccionFormulario(titulo: String) {
    Spacer(Modifier.height(14.dp))
    Text(titulo, style = MaterialTheme.typography.titleLarge, color = Primario)
    Spacer(Modifier.height(4.dp))
    HorizontalDivider(color = BordeSuave)
    Spacer(Modifier.height(8.dp))
}

/** Selector desplegable genérico para enums con etiqueta. */
@Composable
fun SelectorEnum(
    titulo: String,
    opciones: List<Any>,
    seleccion: String,
    alSeleccionar: (Any) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$titulo: $seleccion")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { op ->
                val texto = when (op) {
                    is TipoOportunidad -> op.etiqueta
                    is FuenteInformacion -> op.etiqueta
                    is Prioridad -> op.etiqueta
                    else -> op.toString()
                }
                DropdownMenuItem(text = { Text(texto) },
                    onClick = { alSeleccionar(op); abierto = false })
            }
        }
    }
}
