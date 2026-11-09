package cl.duoc.oportunidades.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.data.local.ContactoEntity
import cl.duoc.oportunidades.data.local.OrganizacionEntity
import cl.duoc.oportunidades.data.model.*
import cl.duoc.oportunidades.ui.theme.*
import cl.duoc.oportunidades.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizacionesScreen(nav: NavHostController, vm: AppViewModel = viewModel()) {
    val organizaciones by vm.organizaciones.collectAsState()
    val guardado by vm.guardado.collectAsState()

    var mostrarNuevaOrg by remember { mutableStateOf(false) }
    var mostrarNuevoContacto by remember { mutableStateOf<OrganizacionEntity?>(null) }
    var orgExpandida by remember { mutableStateOf<Long?>(null) }

    AutoLimpiarBanner(guardado.exito, guardado.error) { vm.limpiarGuardado() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organizaciones y contactos") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarNuevaOrg = true },
                containerColor = Primario, contentColor = Superficie) {
                Icon(Icons.Default.AddBusiness, contentDescription = "Nueva organización")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BannerResultado(guardado.exito, guardado.error) { vm.limpiarGuardado() }

            if (organizaciones.isEmpty()) {
                EstadoVacio(Icons.Default.Apartment, "No hay organizaciones registradas")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(organizaciones, key = { it.id }) { org ->
                        Card(onClick = {
                            orgExpandida = if (orgExpandida == org.id) null else org.id
                        }, colors = CardDefaults.cardColors(containerColor = Superficie),
                            modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(org.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("${org.tipo.etiqueta} · ${org.rubro}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextoSecundario)
                                Text("${org.comuna}, ${org.region}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextoSecundario)

                                // Animación 2: expansión de contactos
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = orgExpandida == org.id
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Contactos", style = MaterialTheme.typography.titleMedium,
                                            color = Primario)
                                        ContactosDeOrganizacion(org.id, vm)
                                        TextButton(onClick = { mostrarNuevoContacto = org }) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Agregar contacto ficticio")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarNuevaOrg) {
        DialogoOrganizacion(
            alGuardar = { org -> vm.guardarOrganizacion(org); mostrarNuevaOrg = false },
            alCancelar = { mostrarNuevaOrg = false }
        )
    }
    mostrarNuevoContacto?.let { org ->
        DialogoContacto(
            organizacionId = org.id,
            alGuardar = { c ->
                val errores = vm.validarContacto(mapOf(
                    "nombre" to c.nombre, "cargo" to c.cargo,
                    "correo" to c.correo, "telefono" to c.telefono))
                if (errores.isEmpty()) {
                    vm.guardarContacto(c, esNuevo = true)
                    mostrarNuevoContacto = null
                }
                errores
            },
            alCancelar = { mostrarNuevoContacto = null }
        )
    }
}

@Composable
fun ContactosDeOrganizacion(orgId: Long, vm: AppViewModel) {
    val contactos by remember(orgId) { vm.contactos(orgId) }.collectAsState()
    if (contactos.isEmpty()) {
        Text("Sin contactos registrados", style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario)
    } else {
        contactos.forEach { c ->
            Card(colors = CardDefaults.cardColors(containerColor = Fondo),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp)) {
                    Text("${c.nombre} — ${c.cargo}", style = MaterialTheme.typography.titleMedium)
                    Text("${c.area.etiqueta}", style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario)
                    Text(c.correo, style = MaterialTheme.typography.bodyMedium)
                    Text(c.telefono, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun DialogoOrganizacion(
    alGuardar: (OrganizacionEntity) -> Unit,
    alCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var rubro by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var sitioWeb by remember { mutableStateOf("") }
    var publico by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoOrganizacion.INSTITUCION_PRIVADA) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Nueva organización (ficticia)") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CampoTexto(nombre, { nombre = it }, "Nombre", obligatorio = true)
                CampoTexto(rubro, { rubro = it }, "Rubro", obligatorio = true)
                CampoTexto(comuna, { comuna = it }, "Comuna", obligatorio = true)
                CampoTexto(region, { region = it }, "Región", obligatorio = true)
                CampoTexto(sitioWeb, { sitioWeb = it }, "Sitio web ficticio")
                CampoTexto(publico, { publico = it }, "Público asociado")
                CampoTexto(observaciones, { observaciones = it }, "Observaciones")
                SelectorEnum("Tipo", TipoOrganizacion.values().toList(),
                    tipo.etiqueta, { tipo = it as TipoOrganizacion })
                error?.let { Text(it, color = ErrorColor) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isBlank() || rubro.isBlank() || comuna.isBlank() || region.isBlank()) {
                    error = "Nombre, rubro, comuna y región son obligatorios"
                } else {
                    alGuardar(OrganizacionEntity(nombre = nombre.trim(), tipo = tipo,
                        rubro = rubro.trim(), comuna = comuna.trim(), region = region.trim(),
                        sitioWeb = sitioWeb.trim(), publicoAsociado = publico.trim(),
                        observaciones = observaciones.trim()))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoContacto(
    organizacionId: Long,
    alGuardar: (ContactoEntity) -> Map<String, String>,
    alCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cargo by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var medio by remember { mutableStateOf("Correo") }
    var area by remember { mutableStateOf(AreaContacto.EVENTOS) }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Nuevo contacto (ficticio)") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CampoTexto(nombre, { nombre = it }, "Nombre", error = errores["nombre"],
                    obligatorio = true)
                CampoTexto(cargo, { cargo = it }, "Cargo o función", error = errores["cargo"],
                    obligatorio = true)
                CampoTexto(correo, { correo = it }, "Correo ficticio",
                    error = errores["correo"], obligatorio = true,
                    placeholder = "nombre@ejemplo.cl")
                CampoTexto(telefono, { telefono = it }, "Teléfono ficticio",
                    error = errores["telefono"], obligatorio = true,
                    placeholder = "+56 9 5555 0101")
                CampoTexto(medio, { medio = it }, "Medio preferente")
                SelectorEnum("Área", AreaContacto.values().toList(), area.etiqueta,
                    { area = it as AreaContacto })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                errores = alGuardar(ContactoEntity(organizacionId = organizacionId,
                    nombre = nombre.trim(), cargo = cargo.trim(), area = area,
                    correo = correo.trim(), telefono = telefono.trim(),
                    medioPreferente = medio.trim(), estado = "Activo"))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}
