package cl.duoc.oportunidades.data

import kotlinx.coroutines.CancellationException

/** Acceso al backend para organizaciones. Nunca lanza: si no hay conexión, avisa con null/false. */
class OrganizacionRemotaRepository(
    private val api: OrganizacionesApi = RetrofitClient.organizacionesApi
) {
    /** Lista del backend, o null si no se pudo conectar o la respuesta fue un error. */
    suspend fun descargar(): List<OrganizacionDto>? =
        try {
            api.obtenerOrganizaciones()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    /** Envía una organización nueva al backend. true si fue creada (201). */
    suspend fun enviar(dto: OrganizacionDto): Boolean =
        try {
            api.crearOrganizacion(dto).isSuccessful
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
}
