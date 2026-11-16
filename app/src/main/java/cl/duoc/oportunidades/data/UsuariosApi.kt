package cl.duoc.oportunidades.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UsuariosApi {
    @GET("v1/api/usuarios")
    suspend fun obtenerUsuarios(): List<UsuarioDto>

    @POST("v1/api/usuarios/login")
    suspend fun login(@Body body: LoginRequest): Response<UsuarioDto>
}
