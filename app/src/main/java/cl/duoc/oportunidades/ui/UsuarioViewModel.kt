package cl.duoc.oportunidades.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.oportunidades.data.UsuarioDto
import cl.duoc.oportunidades.data.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UsuarioUiState {
    data object Cargando : UsuarioUiState

    data class Exito(
        val usuarios: List<UsuarioDto>
    ) : UsuarioUiState

    data class Error(
        val mensaje: String
    ) : UsuarioUiState
}

class UsuarioViewModel(
    private val repository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UsuarioUiState>(UsuarioUiState.Cargando)
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.value = UsuarioUiState.Cargando
            try {
                _uiState.value = UsuarioUiState.Exito(repository.obtenerUsuarios())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UsuarioUiState.Error(
                    "No se pudo conectar con el servidor. " +
                            "Comprueba la conexión e inténtalo nuevamente."
                )
            }
        }
    }
}
