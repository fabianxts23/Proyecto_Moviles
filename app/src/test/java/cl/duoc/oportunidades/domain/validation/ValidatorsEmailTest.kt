package cl.duoc.oportunidades.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas dedicadas a Validators.email (retorna mensaje de error o null si es válido). */
class ValidatorsEmailTest {

    private val msgObligatorio = "El correo es obligatorio"
    private val msgFormato = "Formato de correo inválido (ej: nombre@ejemplo.cl)"

    // ---------- casos inválidos ----------
    @Test
    fun email_vacio_retornaObligatorio() {
        // ARRANGE
        val correo = ""
        // ACT
        val resultado = Validators.email(correo)
        // ASSERT
        assertEquals(msgObligatorio, resultado)
    }

    @Test
    fun email_soloEspacios_retornaObligatorio() {
        assertEquals(msgObligatorio, Validators.email("   "))
    }

    @Test
    fun email_sinArroba_retornaFormatoInvalido() {
        assertEquals(msgFormato, Validators.email("nombreejemplo.cl"))
    }

    @Test
    fun email_sinUsuarioAntesDeArroba_retornaFormatoInvalido() {
        assertEquals(msgFormato, Validators.email("@ejemplo.cl"))
    }

    @Test
    fun email_sinDominioDespuesDeArroba_retornaFormatoInvalido() {
        assertEquals(msgFormato, Validators.email("nombre@"))
    }

    @Test
    fun email_dominioSinPunto_retornaFormatoInvalido() {
        assertEquals(msgFormato, Validators.email("nombre@ejemplo"))
    }

    @Test
    fun email_extensionDeUnaLetra_retornaFormatoInvalido() {
        // La extensión (.cl, .com) debe tener al menos 2 letras
        assertEquals(msgFormato, Validators.email("nombre@ejemplo.c"))
    }

    @Test
    fun email_conEspacioInterno_retornaFormatoInvalido() {
        assertEquals(msgFormato, Validators.email("nom bre@ejemplo.cl"))
    }

    @Test
    fun email_dosArrobas_retornaFormatoInvalido() {
        assertEquals(msgFormato, Validators.email("a@b@ejemplo.cl"))
    }

    @Test
    fun email_conEspaciosAlrededor_retornaFormatoInvalido() {
        // Validators.email no hace trim: el espacio hace fallar el formato
        assertEquals(msgFormato, Validators.email(" nombre@ejemplo.cl "))
    }

    // ---------- casos válidos ----------
    @Test
    fun email_correcto_retornaNull() {
        assertNull(Validators.email("nombre@ejemplo.cl"))
    }

    @Test
    fun email_conPuntoGuionBajoYMas_retornaNull() {
        assertNull(Validators.email("nombre.apellido_1+tag@ejemplo.cl"))
    }

    @Test
    fun email_conSubdominioYMayusculas_retornaNull() {
        assertNull(Validators.email("Ana.Perez@Mail.Duoc.CL"))
    }
}
