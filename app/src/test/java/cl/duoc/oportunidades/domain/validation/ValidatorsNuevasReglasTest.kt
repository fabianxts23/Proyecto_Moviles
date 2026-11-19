package cl.duoc.oportunidades.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas de las reglas nuevas de Validators: horario estricto, largo máximo y campos opcionales. */
class ValidatorsNuevasReglasTest {

    // ---------- horario (horas reales y término posterior al inicio) ----------
    @Test
    fun horario_horaInexistente_retornaError() {
        assertEquals("Horario inválido: use horas entre 00:00 y 23:59", Validators.horario("25:00 - 26:00"))
        assertEquals("Horario inválido: use horas entre 00:00 y 23:59", Validators.horario("10:60 - 18:00"))
    }

    @Test
    fun horario_terminoAntesOIgualAlInicio_retornaError() {
        val msg = "La hora de término debe ser posterior a la de inicio"
        assertEquals(msg, Validators.horario("18:00 - 10:00"))
        assertEquals(msg, Validators.horario("10:00 - 10:00"))
    }

    @Test
    fun horario_rangoCorrecto_retornaNull() {
        assertNull(Validators.horario("09:00 - 17:30"))
    }

    // ---------- longitudMaxima ----------
    @Test
    fun longitudMaxima_textoMuyLargo_retornaError() {
        assertEquals(
            "El nombre no puede superar los 5 caracteres",
            Validators.longitudMaxima("abcdef", 5, "El nombre")
        )
    }

    @Test
    fun longitudMaxima_enElLimiteONulo_retornaNull() {
        assertNull(Validators.longitudMaxima("abcde", 5, "El nombre"))
        assertNull(Validators.longitudMaxima(null, 5, "El nombre"))
    }

    @Test
    fun longitudMaxima_ignoraEspaciosDeLosExtremos() {
        assertNull(Validators.longitudMaxima("  abcde  ", 5, "El nombre"))
    }

    // ---------- sitioWebOpcional ----------
    @Test
    fun sitioWeb_vacioONulo_retornaNull() {
        assertNull(Validators.sitioWebOpcional(""))
        assertNull(Validators.sitioWebOpcional("   "))
        assertNull(Validators.sitioWebOpcional(null))
    }

    @Test
    fun sitioWeb_formatosValidos_retornaNull() {
        assertNull(Validators.sitioWebOpcional("www.ejemplo.cl"))
        assertNull(Validators.sitioWebOpcional("https://ejemplo.cl/ruta"))
    }

    @Test
    fun sitioWeb_formatoInvalido_retornaError() {
        assertEquals("Sitio web inválido (ej: www.ejemplo.cl)", Validators.sitioWebOpcional("hola"))
        assertNotNull(Validators.sitioWebOpcional("ejemplo"))
        assertNotNull(Validators.sitioWebOpcional("www.ejem plo.cl"))
    }

    // ---------- enteroOpcional ----------
    @Test
    fun enteroOpcional_vacio_retornaNull() {
        assertNull(Validators.enteroOpcional("", "Las inscripciones"))
        assertNull(Validators.enteroOpcional(null, "Las inscripciones"))
    }

    @Test
    fun enteroOpcional_conTexto_seValida() {
        assertNull(Validators.enteroOpcional("12", "Las inscripciones"))
        assertEquals(
            "Las inscripciones debe ser numérico",
            Validators.enteroOpcional("abc", "Las inscripciones")
        )
        assertEquals(
            "Las inscripciones no puede ser negativo",
            Validators.enteroOpcional("-3", "Las inscripciones")
        )
    }

    // ---------- evaluacionOpcional ----------
    @Test
    fun evaluacion_vacia_retornaNull() {
        assertNull(Validators.evaluacionOpcional(""))
        assertNull(Validators.evaluacionOpcional(null))
    }

    @Test
    fun evaluacion_fueraDeRango_retornaError() {
        assertEquals("La evaluación debe estar entre 1 y 5", Validators.evaluacionOpcional("0"))
        assertEquals("La evaluación debe estar entre 1 y 5", Validators.evaluacionOpcional("6"))
    }

    @Test
    fun evaluacion_noNumerica_retornaError() {
        assertEquals("La evaluación debe ser un número del 1 al 5", Validators.evaluacionOpcional("x"))
    }

    @Test
    fun evaluacion_limitesValidos_retornaNull() {
        assertNull(Validators.evaluacionOpcional("1"))
        assertNull(Validators.evaluacionOpcional("5"))
    }
}
