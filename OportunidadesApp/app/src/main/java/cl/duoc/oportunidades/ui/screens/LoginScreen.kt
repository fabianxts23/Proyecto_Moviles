package cl.duoc.oportunidades.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.oportunidades.ui.navigation.Rutas
import cl.duoc.oportunidades.ui.theme.*
import cl.duoc.oportunidades.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(nav: NavHostController, vm: AuthViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()

    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }

    // Navegar al dashboard cuando la autenticación es exitosa
    LaunchedEffect(ui.autenticado) {
        if (ui.autenticado) {
            nav.navigate(Rutas.DASHBOARD) {
                popUpTo(Rutas.LOGIN) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo con animación de escala (mecanismo de animación 1)
        val escala by animateFloatAsState(
            targetValue = if (ui.cargando) 0.9f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "logo"
        )
        Surface(
            color = Primario,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.size(88.dp).scale(escala)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.BusinessCenter, contentDescription = null,
                    tint = Superficie, modifier = Modifier.size(44.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Oportunidades Corporativas", style = MaterialTheme.typography.headlineMedium,
            color = Primario, fontWeight = FontWeight.Bold)
        Text("Gestión móvil de activaciones y convenios",
            style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)

        Spacer(Modifier.height(28.dp))
        CampoTexto(correo, { correo = it; vm.limpiarError() }, "Correo",
            obligatorio = true, placeholder = "ejecutivo1@ficticio.cl")
        Spacer(Modifier.height(10.dp))
        CampoTexto(clave, { clave = it; vm.limpiarError() }, "Contraseña",
            obligatorio = true, esClave = true, placeholder = "duoc123")

        if (ui.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(ui.error!!, color = ErrorColor, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { vm.login(correo, clave) },
            enabled = !ui.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (ui.cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp),
                    color = Superficie, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(if (ui.cargando) "Ingresando..." else "Iniciar sesión")
        }

        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = PrimarioContenedor)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Cuentas ficticias de prueba", style = MaterialTheme.typography.titleMedium)
                Text("Supervisor: supervisor@ficticio.cl / duoc123",
                    style = MaterialTheme.typography.bodyMedium)
                Text("Ejecutivos: ejecutivo1@ficticio.cl (1, 2 y 3) / duoc123",
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
