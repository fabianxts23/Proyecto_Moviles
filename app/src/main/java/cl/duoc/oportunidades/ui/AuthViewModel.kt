package cl.duoc.oportunidades.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.oportunidades.data.OportunidadRepository
import cl.duoc.oportunidades.data.SessionManager
import cl.duoc.oportunidades.data.local.AppDatabase
import cl.duoc.oportunidades.data.ResultadoLoginRemoto
import cl.duoc.oportunidades.data.UsuarioRepository
import cl.duoc.oportunidades.data.local.UsuarioEntity
import cl.duoc.oportunidades.data.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val cargando: Boolean = false,
    val autenticado: Boolean = false,
    val error: String? = null,
    val rol: Rol? = null,
    val usuario: UsuarioEntity? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.obtener(app)
    private val repo = OportunidadRepository(db)
    private val usuariosRemoto = UsuarioRepository()
    val session = SessionManager(app)

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    init {
        // Restaura la sesión si el usuario no cerró sesión
        if (session.restaurarSesion()) {
            _ui.value = AuthUiState(autenticado = true, rol = session.rol,
                usuario = session.usuarioActual)
        }
    }

    fun login(correo: String, password: String) {
        if (correo.isBlank() || password.isBlank()) {
            _ui.value = AuthUiState(error = "Ingrese correo y contraseña")
            return
        }
        _ui.value = AuthUiState(cargando = true)
        viewModelScope.launch {
            val usuario = try {
                // 1) Backend; 2) si no hay conexión, respaldo local (Room)
                when (val remoto = usuariosRemoto.login(correo, password)) {
                    is ResultadoLoginRemoto.Ok ->
                        repo.sincronizarUsuario(remoto.usuario.aEntidadLocal(password))
                    ResultadoLoginRemoto.Rechazado -> null
                    ResultadoLoginRemoto.SinConexion -> repo.login(correo, password)
                }
            } catch (e: Exception) {
                _ui.value = AuthUiState(error = "No se pudo acceder a la base de datos")
                return@launch
            }
            if (usuario == null) {
                _ui.value = AuthUiState(error = "Credenciales incorrectas o cuenta inactiva")
            } else {
                session.guardarSesion(usuario)
                _ui.value = AuthUiState(autenticado = true, rol = usuario.rol, usuario = usuario)
            }
        }
    }

    fun logout() {
        session.cerrarSesion()
        _ui.value = AuthUiState()
    }

    fun limpiarError() { _ui.value = _ui.value.copy(error = null) }
}
