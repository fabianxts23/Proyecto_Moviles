package cl.duoc.oportunidades.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la regla que decide qué organizaciones del backend se agregan a la app. */
class OrganizacionSyncTest {

    private fun nuevas(remotas: List<String>, locales: List<String>) =
        OrganizacionSync.nuevas(remotas, { it }, locales)

    @Test
    fun clave_ignoraMayusculasYEspacios() {
        assertEquals("municipalidad", OrganizacionSync.clave("  Municipalidad "))
    }

    @Test
    fun nuevas_agregaLasQueLaAppNoTiene() {
        val r = nuevas(listOf("Org A", "Org B"), listOf("Org A"))
        assertEquals(listOf("Org B"), r)
    }

    @Test
    fun nuevas_noDuplicaAunqueCambienMayusculasOEspacios() {
        val r = nuevas(listOf("  ORG a "), listOf("Org A"))
        assertTrue(r.isEmpty())
    }

    @Test
    fun nuevas_noRepiteLasDuplicadasDelPropioBackend() {
        val r = nuevas(listOf("Org X", "org x", "Org Y"), emptyList())
        assertEquals(listOf("Org X", "Org Y"), r)
    }

    @Test
    fun nuevas_ignoraNombresVacios() {
        val r = nuevas(listOf("", "   ", "Org Z"), emptyList())
        assertEquals(listOf("Org Z"), r)
    }

    @Test
    fun nuevas_sinDatosRemotos_retornaVacio() {
        assertTrue(nuevas(emptyList(), listOf("Org A")).isEmpty())
    }

    @Test
    fun nuevas_appVacia_agregaTodas() {
        assertEquals(listOf("A", "B"), nuevas(listOf("A", "B"), emptyList()))
    }
}
