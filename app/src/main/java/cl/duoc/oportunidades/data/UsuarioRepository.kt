package cl.duoc.oportunidades.data

import java.io.IOException

/** Resultado de intentar autenticarse contra el backend. */
sealed interface ResultadoLoginRemoto {
    data class Ok(val usuario: UsuarioDto) : ResultadoLoginRemoto
    /** El servidor respondió y rechazó las credenciales (o cuenta inactiva). */
    data object Rechazado : ResultadoLoginRemoto
    /** No se pudo hablar con el servidor (caído, sin red, error 5xx). */
    data object SinConexion : ResultadoLoginRemoto
}

class UsuarioRepository(
    private val api: UsuariosApi = RetrofitClient.api
) {
    suspend fun obtenerUsuarios(): List<UsuarioDto> = api.obtenerUsuarios()

    suspend fun login(correo: String, password: String): ResultadoLoginRemoto =
        try {
            val resp = api.login(LoginRequest(correo.trim().lowercase(), password))
            val cuerpo = resp.body()
            when {
                resp.isSuccessful && cuerpo != null -> ResultadoLoginRemoto.Ok(cuerpo)
                resp.code() in 400..499 -> ResultadoLoginRemoto.Rechazado
                else -> ResultadoLoginRemoto.SinConexion
            }
        } catch (e: IOException) {
            ResultadoLoginRemoto.SinConexion
        } catch (e: com.google.gson.JsonParseException) {
            ResultadoLoginRemoto.SinConexion
        }
}
