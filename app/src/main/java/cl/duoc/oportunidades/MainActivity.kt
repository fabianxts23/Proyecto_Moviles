package cl.duoc.oportunidades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import cl.duoc.oportunidades.data.SessionManager
import cl.duoc.oportunidades.ui.navigation.AppNavHost
import cl.duoc.oportunidades.ui.navigation.Rutas
import cl.duoc.oportunidades.ui.theme.OportunidadesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Punto de arranque de la app: decide si ir al login o al dashboard
        // según si existe una sesión previa.
        val sesion = SessionManager(this)
        val inicio = if (sesion.restaurarSesion()) Rutas.DASHBOARD else Rutas.LOGIN

        setContent {
            OportunidadesTheme {
                val nav = rememberNavController()
                AppNavHost(nav = nav, startDestino = inicio)
            }
        }
    }
}
