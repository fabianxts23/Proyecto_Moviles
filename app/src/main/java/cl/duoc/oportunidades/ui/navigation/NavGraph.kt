package cl.duoc.oportunidades.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.oportunidades.ui.screens.*

object Rutas {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val OPORTUNIDADES = "oportunidades"
    const val FORM = "form/{id}"
    const val DETALLE = "detalle/{id}"
    const val ORGANIZACIONES = "organizaciones"
    const val SEGUIMIENTO = "seguimiento/{id}"
    const val EVIDENCIAS = "evidencias/{id}"
    const val SUPERVISOR = "supervisor"

    fun form(id: Long?) = if (id == null) "form/-1" else "form/$id"
    fun detalle(id: Long) = "detalle/$id"
    fun seguimiento(id: Long) = "seguimiento/$id"
    fun evidencias(id: Long) = "evidencias/$id"
}

@Composable
fun AppNavHost(nav: NavHostController, startDestino: String) {
    NavHost(navController = nav, startDestination = startDestino) {

        composable(Rutas.LOGIN) { LoginScreen(nav) }

        composable(Rutas.DASHBOARD) { DashboardScreen(nav) }

        composable(Rutas.OPORTUNIDADES) { OportunidadesScreen(nav) }

        composable(
            Rutas.FORM,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: -1L
            OportunidadFormScreen(nav, if (id == -1L) null else id)
        }

        composable(
            Rutas.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            OportunidadDetalleScreen(nav, entry.arguments?.getLong("id") ?: 0L)
        }

        composable(Rutas.ORGANIZACIONES) { OrganizacionesScreen(nav) }

        composable(
            Rutas.SEGUIMIENTO,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            SeguimientoScreen(nav, entry.arguments?.getLong("id") ?: 0L)
        }

        composable(
            Rutas.EVIDENCIAS,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            EvidenciasScreen(nav, entry.arguments?.getLong("id") ?: 0L)
        }

        composable(Rutas.SUPERVISOR) { SupervisorScreen(nav) }
    }
}
