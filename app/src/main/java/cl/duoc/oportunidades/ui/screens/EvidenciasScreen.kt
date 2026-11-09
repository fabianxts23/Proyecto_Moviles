package cl.duoc.oportunidades.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.camera.CameraUtils
import cl.duoc.oportunidades.ui.theme.*
import cl.duoc.oportunidades.ui.viewmodel.AppViewModel
import coil.compose.AsyncImage

/**
 * Evidencias fotográficas de una oportunidad/evento.
 * Usa la cámara del dispositivo, muestra vista previa y conserva
 * la foto asociada a su oportunidad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenciasScreen(
    nav: NavHostController,
    id: Long,
    vm: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    val fotos by remember(id) { vm.fotos(id) }.collectAsState()

    var uriPendiente by rememberSaveable { mutableStateOf<android.net.Uri?>(null) }
    var ultimaCapturada by remember { mutableStateOf<android.net.Uri?>(null) }

    // Launcher de la cámara
    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            uriPendiente?.let { uriCache ->
                val uriFinal = CameraUtils.conservarFoto(context, uriCache)
                vm.guardarFoto(id, uriFinal)
                ultimaCapturada = uriFinal
            }
        }
        uriPendiente = null
    }

    // Launcher de permisos
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val uri = CameraUtils.crearUriFoto(context)
            uriPendiente = uri
            lanzadorCamara.launch(uri)
        }
    }

    fun dispararCamara() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                val uri = CameraUtils.crearUriFoto(context)
                uriPendiente = uri
                lanzadorCamara.launch(uri)
            }
            else -> lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evidencias fotográficas") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { dispararCamara() },
                containerColor = Primario, contentColor = Superficie) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Capturar foto")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Vista previa de la última fotografía capturada
            ultimaCapturada?.let { uri ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Última captura", style = MaterialTheme.typography.titleMedium,
                            color = Primario, modifier = Modifier.padding(10.dp))
                        AsyncImage(
                            model = uri,
                            contentDescription = "Vista previa",
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Text("Fotos de la oportunidad (${fotos.size})",
                style = MaterialTheme.typography.titleLarge)

            if (fotos.isEmpty()) {
                EstadoVacio(Icons.Default.PhotoCamera,
                    "Sin evidencias. Usa el botón para capturar la primera foto.")
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()) {
                    items(fotos, key = { it.id }) { f ->
                        Card {
                            AsyncImage(
                                model = f.uri,
                                contentDescription = "Evidencia",
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
