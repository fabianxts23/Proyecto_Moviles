package cl.duoc.oportunidades.data

import cl.duoc.oportunidades.data.local.OrganizacionEntity
import cl.duoc.oportunidades.data.model.TipoOrganizacion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Endpoints de organizaciones del backend (Spring Boot + MySQL). */
interface OrganizacionesApi {
    @GET("v1/api/organizaciones")
    suspend fun obtenerOrganizaciones(): List<OrganizacionDto>

    @POST("v1/api/organizaciones")
    suspend fun crearOrganizacion(@Body body: OrganizacionDto): Response<OrganizacionDto>
}

/** Organización tal como la entrega/recibe el backend (mismos nombres de campo que su JSON). */
data class OrganizacionDto(
    val id: Long? = null,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String? = null,
    val comuna: String? = null,
    val region: String? = null,
    val sitioWeb: String? = null,
    val publicoAsociado: String? = null,
    val observaciones: String? = null
) {
    /** Convierte a la entidad local. El id local lo asigna Room (el de MySQL es distinto). */
    fun aEntidadLocal() = OrganizacionEntity(
        id = 0,
        nombre = nombre.trim(),
        tipo = tipo,
        rubro = rubro ?: "",
        comuna = comuna ?: "",
        region = region ?: "",
        sitioWeb = sitioWeb ?: "",
        publicoAsociado = publicoAsociado ?: "",
        observaciones = observaciones ?: ""
    )

    companion object {
        /** Arma el cuerpo para enviar una organización creada en la app (sin id: lo asigna MySQL). */
        fun desde(o: OrganizacionEntity) = OrganizacionDto(
            id = null,
            nombre = o.nombre,
            tipo = o.tipo,
            rubro = o.rubro.ifBlank { null },
            comuna = o.comuna.ifBlank { null },
            region = o.region.ifBlank { null },
            sitioWeb = o.sitioWeb.ifBlank { null },
            publicoAsociado = o.publicoAsociado.ifBlank { null },
            observaciones = o.observaciones.ifBlank { null }
        )
    }
}
