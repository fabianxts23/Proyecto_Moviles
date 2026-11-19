package cl.duoc.oportunidades.data

import cl.duoc.oportunidades.data.model.Rol
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/**
 * Pruebas de UsuarioRepository con una API falsa escrita a mano
 * (sin red real y sin librerías de mocking).
 */
class UsuarioRepositoryTest {

    private val usuario = UsuarioDto(1, "Ana", "ana@x.cl", Rol.EJECUTIVO)

    /** API falsa: cada prueba define qué responde (o qué excepción lanza). */
    private class ApiFalsa(
        private val loginHandler: () -> Response<UsuarioDto>,
        private val usuarios: List<UsuarioDto> = emptyList()
    ) : UsuariosApi {
        var ultimoRequest: LoginRequest? = null
        override suspend fun obtenerUsuarios(): List<UsuarioDto> = usuarios
        override suspend fun login(body: LoginRequest): Response<UsuarioDto> {
            ultimoRequest = body
            return loginHandler()
        }
    }

    private fun error(codigo: Int): Response<UsuarioDto> =
        Response.error(codigo, "".toResponseBody("application/json".toMediaType()))

    @Test
    fun login_respuestaExitosa_retornaOk() = runBlocking {
        val repo = UsuarioRepository(ApiFalsa({ Response.success(usuario) }))
        val r = repo.login("ana@x.cl", "123")
        assertEquals(ResultadoLoginRemoto.Ok(usuario), r)
    }

    @Test
    fun login_normalizaCorreoAntesDeEnviar() = runBlocking {
        val api = ApiFalsa({ Response.success(usuario) })
        UsuarioRepository(api).login("  ANA@X.cl ", "123")
        assertEquals("ana@x.cl", api.ultimoRequest?.correo)
        assertEquals("123", api.ultimoRequest?.password)
    }

    @Test
    fun login_error401_retornaRechazado() = runBlocking {
        val r = UsuarioRepository(ApiFalsa({ error(401) })).login("a@x.cl", "mala")
        assertEquals(ResultadoLoginRemoto.Rechazado, r)
    }

    @Test
    fun login_error500_retornaSinConexion() = runBlocking {
        val r = UsuarioRepository(ApiFalsa({ error(500) })).login("a@x.cl", "p")
        assertEquals(ResultadoLoginRemoto.SinConexion, r)
    }

    @Test
    fun login_exitoConCuerpoNulo_retornaSinConexion() = runBlocking {
        val r = UsuarioRepository(ApiFalsa({ Response.success<UsuarioDto>(null) })).login("a@x.cl", "p")
        assertEquals(ResultadoLoginRemoto.SinConexion, r)
    }

    @Test
    fun login_ioException_retornaSinConexion() = runBlocking {
        val r = UsuarioRepository(ApiFalsa({ throw IOException("sin red") })).login("a@x.cl", "p")
        assertEquals(ResultadoLoginRemoto.SinConexion, r)
    }

    @Test
    fun login_jsonInvalido_retornaSinConexion() = runBlocking {
        val r = UsuarioRepository(ApiFalsa({ throw com.google.gson.JsonParseException("json roto") }))
            .login("a@x.cl", "p")
        assertEquals(ResultadoLoginRemoto.SinConexion, r)
    }

    @Test
    fun obtenerUsuarios_delegaEnLaApi() = runBlocking {
        val repo = UsuarioRepository(ApiFalsa({ error(500) }, listOf(usuario)))
        assertTrue(repo.obtenerUsuarios().contains(usuario))
    }
}
