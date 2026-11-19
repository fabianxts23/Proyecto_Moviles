package cl.duoc.oportunidades.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas de negocio de cambio de estado según el rol. */
class ReglasEstadoTest {

    // ---------- validar ----------
    @Test
    fun validar_estadoActualFinal_retornaErrorDeCerrada() {
        val r = ReglasEstado.validar(
            EstadoOportunidad.OPORTUNIDAD_DESCARTADA,
            EstadoOportunidad.CONTACTO_PENDIENTE,
            Rol.SUPERVISOR
        )
        assertNotNull(r)
        assertTrue(r!!.contains("cerrada"))
    }

    @Test
    fun validar_mismoEstado_retornaError() {
        val r = ReglasEstado.validar(
            EstadoOportunidad.EN_EVALUACION, EstadoOportunidad.EN_EVALUACION, Rol.EJECUTIVO
        )
        assertEquals("La oportunidad ya se encuentra en el estado En evaluación", r)
    }

    @Test
    fun validar_estadoFinalComoEjecutivo_retornaError() {
        val r = ReglasEstado.validar(
            EstadoOportunidad.PROPUESTA_ENVIADA,
            EstadoOportunidad.PARTICIPACION_CONFIRMADA,
            Rol.EJECUTIVO
        )
        assertNotNull(r)
        assertTrue(r!!.contains("supervisor"))
    }

    @Test
    fun validar_estadoFinalComoMarketing_retornaError() {
        assertNotNull(
            ReglasEstado.validar(
                EstadoOportunidad.EN_EVALUACION, EstadoOportunidad.ACTIVACION_CANCELADA, Rol.MARKETING
            )
        )
    }

    @Test
    fun validar_estadoFinalComoSupervisor_retornaNull() {
        assertNull(
            ReglasEstado.validar(
                EstadoOportunidad.EN_EVALUACION, EstadoOportunidad.PARTICIPACION_CONFIRMADA, Rol.SUPERVISOR
            )
        )
    }

    @Test
    fun validar_activoAActivoComoEjecutivo_retornaNull() {
        assertNull(
            ReglasEstado.validar(
                EstadoOportunidad.EVENTO_IDENTIFICADO, EstadoOportunidad.CONTACTO_REALIZADO, Rol.EJECUTIVO
            )
        )
    }

    // ---------- transicionesPermitidas ----------
    @Test
    fun transiciones_desdeEstadoFinal_retornaListaVacia() {
        for (rol in Rol.values()) {
            assertTrue(
                ReglasEstado.transicionesPermitidas(EstadoOportunidad.ACTIVACION_REALIZADA, rol).isEmpty()
            )
        }
    }

    @Test
    fun transiciones_ejecutivo_soloEstadosActivosYSinElActual() {
        val actual = EstadoOportunidad.CONTACTO_PENDIENTE
        val permitidas = ReglasEstado.transicionesPermitidas(actual, Rol.EJECUTIVO)
        assertFalse(permitidas.contains(actual))
        assertTrue(permitidas.none { it.esFinal })
        assertTrue(permitidas.contains(EstadoOportunidad.PROPUESTA_ENVIADA))
    }

    @Test
    fun transiciones_supervisor_incluyeEstadosFinales() {
        val permitidas = ReglasEstado.transicionesPermitidas(EstadoOportunidad.CONTACTO_PENDIENTE, Rol.SUPERVISOR)
        assertTrue(permitidas.contains(EstadoOportunidad.PARTICIPACION_CONFIRMADA))
        assertTrue(permitidas.contains(EstadoOportunidad.OPORTUNIDAD_DESCARTADA))
        assertFalse(permitidas.contains(EstadoOportunidad.CONTACTO_PENDIENTE))
        // 12 estados - el actual = 11
        assertEquals(11, permitidas.size)
    }

    @Test
    fun transiciones_coincidenConValidar() {
        // Coherencia: toda transición listada debe pasar la validación (y viceversa)
        for (rol in Rol.values()) {
            for (actual in EstadoOportunidad.values()) {
                val permitidas = ReglasEstado.transicionesPermitidas(actual, rol)
                for (nuevo in EstadoOportunidad.values()) {
                    assertEquals(
                        permitidas.contains(nuevo),
                        ReglasEstado.validar(actual, nuevo, rol) == null
                    )
                }
            }
        }
    }

    // ---------- enum ----------
    @Test
    fun estadosFinales_sonExactamenteCuatro() {
        val finales = EstadoOportunidad.values().filter { it.esFinal }
        assertEquals(4, finales.size)
    }
}
