package cl.duoc.oportunidades.data.local

import cl.duoc.oportunidades.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

/** Los enums se guardan en Room como Int (ordinal): ida y vuelta debe devolver el mismo valor. */
class ConvertersTest {

    private val c = Converters()

    @Test
    fun rol_ordinalIdaYVuelta() {
        for (v in Rol.values()) assertEquals(v, c.intToRol(c.rolToInt(v)))
    }

    @Test
    fun estado_ordinalIdaYVuelta() {
        for (v in EstadoOportunidad.values()) assertEquals(v, c.intToEstado(c.estadoToInt(v)))
    }

    @Test
    fun prioridad_ordinalIdaYVuelta() {
        for (v in Prioridad.values()) assertEquals(v, c.intToPrioridad(c.prioridadToInt(v)))
    }

    @Test
    fun tipoOportunidad_ordinalIdaYVuelta() {
        for (v in TipoOportunidad.values()) assertEquals(v, c.intToTipoOportunidad(c.tipoOportunidadToInt(v)))
    }

    @Test
    fun otrosEnums_ordinalIdaYVuelta() {
        for (v in TipoOrganizacion.values()) assertEquals(v, c.intToTipoOrganizacion(c.tipoOrganizacionToInt(v)))
        for (v in AreaContacto.values()) assertEquals(v, c.intToArea(c.areaToInt(v)))
        for (v in TipoAccion.values()) assertEquals(v, c.intToTipoAccion(c.tipoAccionToInt(v)))
        for (v in FuenteInformacion.values()) assertEquals(v, c.intToFuente(c.fuenteToInt(v)))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun rol_ordinalInexistente_lanzaExcepcion() {
        c.intToRol(99)
    }
}
