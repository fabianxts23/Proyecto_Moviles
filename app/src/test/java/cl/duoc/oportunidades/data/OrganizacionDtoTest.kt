package cl.duoc.oportunidades.data

import cl.duoc.oportunidades.data.local.OrganizacionEntity
import cl.duoc.oportunidades.data.model.TipoOrganizacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas de la conversión de organizaciones entre el backend y la base local. */
class OrganizacionDtoTest {

    @Test
    fun aEntidadLocal_copiaCamposYNoUsaElIdDelBackend() {
        val dto = OrganizacionDto(id = 99, nombre = "  Mi Org ", tipo = TipoOrganizacion.ASOCIACION,
            rubro = "Retail", comuna = "Maipú", region = "RM", sitioWeb = "www.x.cl",
            publicoAsociado = "Socios", observaciones = "Obs")

        val e = dto.aEntidadLocal()

        assertEquals(0L, e.id)
        assertEquals("Mi Org", e.nombre)
        assertEquals(TipoOrganizacion.ASOCIACION, e.tipo)
        assertEquals("Retail", e.rubro)
        assertEquals("www.x.cl", e.sitioWeb)
    }

    @Test
    fun aEntidadLocal_camposNulos_quedanVacios() {
        val e = OrganizacionDto(nombre = "Org", tipo = TipoOrganizacion.OTRA).aEntidadLocal()
        assertEquals("", e.rubro)
        assertEquals("", e.comuna)
        assertEquals("", e.region)
        assertEquals("", e.sitioWeb)
        assertEquals("", e.publicoAsociado)
        assertEquals("", e.observaciones)
    }

    @Test
    fun desde_enviaSinIdYConvierteVaciosEnNulo() {
        val local = OrganizacionEntity(id = 7, nombre = "Org", tipo = TipoOrganizacion.OTRA,
            rubro = "Retail", comuna = "", region = "", sitioWeb = "", publicoAsociado = "",
            observaciones = "")

        val dto = OrganizacionDto.desde(local)

        assertNull(dto.id)
        assertEquals("Org", dto.nombre)
        assertEquals("Retail", dto.rubro)
        assertNull(dto.comuna)
        assertNull(dto.sitioWeb)
    }
}
