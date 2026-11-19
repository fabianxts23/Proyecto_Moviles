package cl.duoc.oportunidades.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Emulador: 10.0.2.2 es el PC anfitrión. Celular físico: usar la IP del PC (ej. http://192.168.1.50:8080/)
    // y agregarla en res/xml/network_security_config.xml.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Timeouts cortos: si el servidor no está, la app cae rápido al login local.
    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: UsuariosApi by lazy { retrofit.create(UsuariosApi::class.java) }

    val organizacionesApi: OrganizacionesApi by lazy {
        retrofit.create(OrganizacionesApi::class.java)
    }
}
