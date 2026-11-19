package cl.duoc.oportunidades.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas de las reglas de validación de formularios (retornan mensaje o null). */
class ValidatorsTest {

    // ---------- requerido ----------
    @Test
    fun requerido_valorVacio_retornaError() {
        assertEquals("El nombre es obligatorio", Validators.requerido("", "El nombre"))
    }

    @Test
    fun requerido_soloEspaciosONulo_retornaError() {
        assertNotNull(Validators.requerido("   ", "El nombre"))
        assertNotNull(Validators.requerido(null))
    }

    @Test
    fun requerido_valorCorrecto_retornaNull() {
        assertNull(Validators.requerido("Ana", "El nombre"))
    }

    // ---------- email ----------
    @Test
    fun email_vacio_retornaObligatorio() {
        assertEquals("El correo es obligatorio", Validators.email(" "))
    }

    @Test
    fun email_formatoInvalido_retornaError() {
        assertNotNull(Validators.email("sinArroba.cl"))
        assertNotNull(Validators.email("a@b"))
    }

    @Test
    fun email_correcto_retornaNull() {
        assertNull(Validators.email("nombre@ejemplo.cl"))
    }

    // ---------- telefono ----------
    @Test
    fun telefono_vacio_retornaObligatorio() {
        assertEquals("El teléfono es obligatorio", Validators.telefono(""))
    }

    @Test
    fun telefono_formatoInvalido_retornaError() {
        assertNotNull(Validators.telefono("12345"))
        assertNotNull(Validators.telefono("8 5555 0101"))
    }

    @Test
    fun telefono_formatosChilenosValidos_retornaNull() {
        assertNull(Validators.telefono("+56 9 5555 0101"))
        assertNull(Validators.telefono("955550101"))
    }

    // ---------- fechaValida ----------
    @Test
    fun fechaValida_vacia_retornaObligatoria() {
        assertEquals("La fecha es obligatoria", Validators.fechaValida(""))
    }

    @Test
    fun fechaValida_formatoIncorrecto_retornaError() {
        assertNotNull(Validators.fechaValida("15/03/2026"))
    }

    @Test
    fun fechaValida_fechaInexistente_retornaError() {
        // Formato correcto pero el día no existe (parser no permisivo)
        assertNotNull(Validators.fechaValida("2026-02-30"))
        assertNotNull(Validators.fechaValida("2026-13-01"))
    }

    @Test
    fun fechaValida_fechaCorrecta_retornaNull() {
        assertNull(Validators.fechaValida("2026-03-15"))
    }

    // ---------- horario ----------
    @Test
    fun horario_vacio_retornaObligatorio() {
        assertEquals("El horario es obligatorio", Validators.horario(""))
    }

    @Test
    fun horario_formatoInvalido_retornaError() {
        assertNotNull(Validators.horario("10:00"))
        assertNotNull(Validators.horario("10-18"))
    }

    @Test
    fun horario_correcto_retornaNull() {
        assertNull(Validators.horario("10:00 - 18:00"))
        assertNull(Validators.horario("10:00-18:00"))
    }

    // ---------- enteroNoNegativo ----------
    @Test
    fun enteroNoNegativo_vacio_retornaObligatorio() {
        assertEquals("Asistentes es obligatorio", Validators.enteroNoNegativo("", "Asistentes"))
    }

    @Test
    fun enteroNoNegativo_noNumerico_retornaError() {
        assertEquals("Asistentes debe ser numérico", Validators.enteroNoNegativo("abc", "Asistentes"))
    }

    @Test
    fun enteroNoNegativo_demasiadoGrande_retornaError() {
        assertEquals(
            "Asistentes es demasiado grande",
            Validators.enteroNoNegativo("99999999999", "Asistentes")
        )
    }

    @Test
    fun enteroNoNegativo_negativo_retornaError() {
        assertEquals("Asistentes no puede ser negativo", Validators.enteroNoNegativo("-1", "Asistentes"))
    }

    @Test
    fun enteroNoNegativo_ceroYPositivo_retornaNull() {
        assertNull(Validators.enteroNoNegativo("0", "Asistentes"))
        assertNull(Validators.enteroNoNegativo("150", "Asistentes"))
    }

    // ---------- hora ----------
    @Test
    fun hora_vacia_retornaObligatoria() {
        assertEquals("La hora es obligatoria", Validators.hora(""))
    }

    @Test
    fun hora_fueraDeRango_retornaError() {
        assertNotNull(Validators.hora("24:00"))
        assertNotNull(Validators.hora("12:60"))
        assertNotNull(Validators.hora("9:30"))
    }

    @Test
    fun hora_limitesValidos_retornaNull() {
        assertNull(Validators.hora("00:00"))
        assertNull(Validators.hora("23:59"))
    }

    // ---------- costoNoNegativo ----------
    @Test
    fun costo_vacio_retornaObligatorio() {
        assertNotNull(Validators.costoNoNegativo(""))
    }

    @Test
    fun costo_noNumerico_retornaError() {
        assertEquals("El costo debe ser numérico", Validators.costoNoNegativo("mucho"))
    }

    @Test
    fun costo_negativo_retornaError() {
        assertEquals("El costo no puede ser negativo", Validators.costoNoNegativo("-5"))
    }

    @Test
    fun costo_cero_y_decimal_retornaNull() {
        assertNull(Validators.costoNoNegativo("0"))
        assertNull(Validators.costoNoNegativo("1500.50"))
    }

    // ---------- coherenciaFechas ----------
    @Test
    fun coherenciaFechas_terminoAntesDeInicio_retornaError() {
        assertEquals(
            "La fecha de término no puede ser anterior al inicio",
            Validators.coherenciaFechas(deteccion = 100, inicio = 200, termino = 150)
        )
    }

    @Test
    fun coherenciaFechas_inicioAntesDeDeteccion_retornaError() {
        assertEquals(
            "El evento no puede iniciar antes de ser detectado",
            Validators.coherenciaFechas(deteccion = 300, inicio = 200, termino = 250)
        )
    }

    @Test
    fun coherenciaFechas_fechasCoherentes_retornaNull() {
        assertNull(Validators.coherenciaFechas(deteccion = 100, inicio = 200, termino = 300))
        // Límites iguales también son válidos
        assertNull(Validators.coherenciaFechas(deteccion = 100, inicio = 100, termino = 100))
    }

    // ---------- potencial ----------
    @Test
    fun potencial_noNumerico_retornaError() {
        assertEquals("Debe ser un número del 1 al 5", Validators.potencial("abc"))
    }

    @Test
    fun potencial_fueraDeRango_retornaError() {
        assertEquals("El potencial debe estar entre 1 y 5", Validators.potencial("0"))
        assertEquals("El potencial debe estar entre 1 y 5", Validators.potencial("6"))
    }

    @Test
    fun potencial_limitesValidos_retornaNull() {
        assertNull(Validators.potencial("1"))
        assertNull(Validators.potencial("5"))
    }

    // ---------- validar (primer error) ----------
    @Test
    fun validar_variosErrores_retornaElPrimero() {
        assertEquals("e1", Validators.validar(null, "e1", "e2"))
    }

    @Test
    fun validar_sinErrores_retornaNull() {
        assertNull(Validators.validar(null, null))
    }
}
