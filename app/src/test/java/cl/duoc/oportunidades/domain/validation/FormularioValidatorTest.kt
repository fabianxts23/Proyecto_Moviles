package cl.duoc.oportunidades.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la validación de formularios completos (campo -> mensaje de error). */
class FormularioValidatorTest {

    private val dia = 24L * 60 * 60 * 1000

    /** Formulario de oportunidad totalmente válido; cada prueba cambia solo un campo. */
    private fun oportunidadValida() = mutableMapOf(
        "nombre" to "Feria Duoc", "descripcion" to "Feria de beneficios",
        "eventoNombre" to "Feria 2027", "eventoTipo" to "Feria",
        "fechaInicio" to "2030-05-10", "fechaTermino" to "2030-05-11",
        "horario" to "10:00 - 18:00", "direccion" to "Av. Siempre Viva 123",
        "comuna" to "Maipú", "region" to "Metropolitana",
        "publicoEsperado" to "Estudiantes", "publicoObjetivo" to "Jóvenes",
        "asistentes" to "200", "condiciones" to "Stand propio", "requisitos" to "Ninguno",
        "costos" to "0", "espacios" to "Stand 3x3", "potencial" to "4"
    )

    private val deteccion = System.currentTimeMillis()

    // ---------- oportunidad ----------
    @Test
    fun oportunidad_formularioCompleto_noTieneErrores() {
        assertTrue(FormularioValidator.oportunidad(oportunidadValida(), deteccion).isEmpty())
    }

    @Test
    fun oportunidad_formularioVacio_marcaTodosLosCampos() {
        val errores = FormularioValidator.oportunidad(emptyMap(), deteccion)
        assertEquals(18, errores.size)
        assertTrue(errores.containsKey("nombre"))
        assertTrue(errores.containsKey("horario"))
        assertTrue(errores.containsKey("potencial"))
    }

    @Test
    fun oportunidad_campoEnBlanco_retornaObligatorio() {
        val c = oportunidadValida().apply { put("nombre", "   ") }
        assertEquals("El nombre es obligatorio", FormularioValidator.oportunidad(c, deteccion)["nombre"])
    }

    @Test
    fun oportunidad_textoDemasiadoLargo_retornaError() {
        val c = oportunidadValida().apply { put("nombre", "a".repeat(101)) }
        assertEquals(
            "El nombre no puede superar los 100 caracteres",
            FormularioValidator.oportunidad(c, deteccion)["nombre"]
        )
    }

    @Test
    fun oportunidad_terminoAntesDelInicio_retornaError() {
        val c = oportunidadValida().apply {
            put("fechaInicio", "2030-05-10"); put("fechaTermino", "2030-05-09")
        }
        assertEquals(
            "La fecha de término no puede ser anterior al inicio",
            FormularioValidator.oportunidad(c, deteccion)["fechaTermino"]
        )
    }

    @Test
    fun oportunidad_inicioAnteriorALaDeteccion_retornaError() {
        val c = oportunidadValida().apply { put("fechaInicio", "2000-01-01"); put("fechaTermino", "2000-01-02") }
        val msg = FormularioValidator.oportunidad(c, deteccion)["fechaInicio"]
        assertTrue(msg!!.startsWith("El evento no puede iniciar antes de ser detectado"))
    }

    @Test
    fun oportunidad_fechaInvalida_noAplicaReglasDeCoherencia() {
        val c = oportunidadValida().apply { put("fechaInicio", "no-es-fecha") }
        val errores = FormularioValidator.oportunidad(c, deteccion)
        assertEquals("Fecha inválida (formato yyyy-MM-dd)", errores["fechaInicio"])
        assertNull(errores["fechaTermino"])
    }

    @Test
    fun oportunidad_horarioInvalido_yPotencialFueraDeRango() {
        val c = oportunidadValida().apply { put("horario", "18:00 - 10:00"); put("potencial", "9") }
        val errores = FormularioValidator.oportunidad(c, deteccion)
        assertTrue(errores.containsKey("horario"))
        assertEquals("El potencial debe estar entre 1 y 5", errores["potencial"])
    }

    @Test
    fun oportunidad_asistentesYCostosNegativos_retornaError() {
        val c = oportunidadValida().apply { put("asistentes", "-5"); put("costos", "-100") }
        val errores = FormularioValidator.oportunidad(c, deteccion)
        assertEquals("El público esperado no puede ser negativo", errores["asistentes"])
        assertEquals("El costo no puede ser negativo", errores["costos"])
    }

    // ---------- contacto ----------
    @Test
    fun contacto_valido_noTieneErrores() {
        val c = mapOf("nombre" to "Ana", "cargo" to "Jefa", "correo" to "ana@ejemplo.cl",
            "telefono" to "+56 9 5555 0101")
        assertTrue(FormularioValidator.contacto(c).isEmpty())
    }

    @Test
    fun contacto_vacio_marcaLosCuatroCampos() {
        val errores = FormularioValidator.contacto(emptyMap())
        assertEquals(setOf("nombre", "cargo", "correo", "telefono"), errores.keys)
    }

    @Test
    fun contacto_correoYTelefonoMalos_retornaFormato() {
        val c = mapOf("nombre" to "Ana", "cargo" to "Jefa", "correo" to "ana@", "telefono" to "123")
        val errores = FormularioValidator.contacto(c)
        assertEquals(setOf("correo", "telefono"), errores.keys)
    }

    // ---------- organización ----------
    @Test
    fun organizacion_conSoloObligatorios_esValida() {
        val c = mapOf("nombre" to "Org", "rubro" to "Retail", "comuna" to "Maipú", "region" to "RM")
        assertTrue(FormularioValidator.organizacion(c).isEmpty())
    }

    @Test
    fun organizacion_sinDatos_marcaObligatorios() {
        val errores = FormularioValidator.organizacion(emptyMap())
        assertEquals(setOf("nombre", "rubro", "comuna", "region"), errores.keys)
    }

    @Test
    fun organizacion_sitioWebInvalido_retornaError() {
        val c = mapOf("nombre" to "Org", "rubro" to "Retail", "comuna" to "Maipú",
            "region" to "RM", "sitioWeb" to "hola")
        assertEquals("Sitio web inválido (ej: www.ejemplo.cl)", FormularioValidator.organizacion(c)["sitioWeb"])
    }

    @Test
    fun organizacion_observacionesMuyLargas_retornaError() {
        val c = mapOf("nombre" to "Org", "rubro" to "Retail", "comuna" to "Maipú",
            "region" to "RM", "observaciones" to "x".repeat(501))
        assertTrue(FormularioValidator.organizacion(c).containsKey("observaciones"))
    }

    @Test
    fun organizacion_comunaORegionConMasDe80Caracteres_retornaError() {
        val c = mapOf("nombre" to "Org", "rubro" to "Retail",
            "comuna" to "c".repeat(81), "region" to "r".repeat(81))
        val errores = FormularioValidator.organizacion(c)
        assertEquals("La comuna no puede superar los 80 caracteres", errores["comuna"])
        assertEquals("La región no puede superar los 80 caracteres", errores["region"])
    }

    @Test
    fun organizacion_sitioWebConMasDe200Caracteres_retornaError() {
        val c = mapOf("nombre" to "Org", "rubro" to "Retail", "comuna" to "Maipú", "region" to "RM",
            "sitioWeb" to "www." + "a".repeat(200) + ".cl")
        assertTrue(FormularioValidator.organizacion(c).containsKey("sitioWeb"))
    }

    @Test
    fun oportunidad_comunaConMasDe80Caracteres_retornaError() {
        val c = oportunidadValida().apply { put("comuna", "c".repeat(81)) }
        assertEquals(
            "La comuna no puede superar los 80 caracteres",
            FormularioValidator.oportunidad(c, deteccion)["comuna"]
        )
    }

    // ---------- resultado de la activación ----------
    @Test
    fun resultado_soloObligatorios_esValido() {
        assertTrue(FormularioValidator.resultado(mapOf("asistentes" to "100", "contactos" to "20")).isEmpty())
    }

    @Test
    fun resultado_sinObligatorios_marcaAsistentesYContactos() {
        val errores = FormularioValidator.resultado(emptyMap())
        assertEquals(setOf("asistentes", "contactos"), errores.keys)
    }

    @Test
    fun resultado_evaluacionFueraDeRango_retornaError() {
        val c = mapOf("asistentes" to "100", "contactos" to "20", "evaluacion" to "7")
        assertEquals("La evaluación debe estar entre 1 y 5", FormularioValidator.resultado(c)["evaluacion"])
    }

    @Test
    fun resultado_opcionalesVaciosOValidos_noTienenErrores() {
        val c = mapOf("asistentes" to "100", "contactos" to "20", "interesados" to "",
            "inscripciones" to "5", "evaluacion" to "")
        assertTrue(FormularioValidator.resultado(c).isEmpty())
    }

    // ---------- seguimiento ----------
    private fun seguimientoValido() = mutableMapOf(
        "fechaAccion" to "2030-05-10", "horaAccion" to "15:30",
        "resultado" to "Se habló con el contacto", "proximaAccion" to "Enviar propuesta",
        "fechaProxima" to "2030-05-17"
    )

    @Test
    fun seguimiento_valido_noTieneErrores() {
        assertTrue(FormularioValidator.seguimiento(seguimientoValido()).isEmpty())
    }

    @Test
    fun seguimiento_vacio_marcaLosCincoCampos() {
        val errores = FormularioValidator.seguimiento(emptyMap())
        assertEquals(
            setOf("fechaAccion", "horaAccion", "resultado", "proximaAccion", "fechaProxima"),
            errores.keys
        )
    }

    @Test
    fun seguimiento_proximaFechaAnterior_retornaError() {
        val c = seguimientoValido().apply { put("fechaProxima", "2030-05-01") }
        assertEquals(
            "La próxima fecha no puede ser anterior a la fecha de la acción",
            FormularioValidator.seguimiento(c)["fechaProxima"]
        )
    }

    @Test
    fun seguimiento_horaInvalida_retornaError() {
        val c = seguimientoValido().apply { put("horaAccion", "25:99") }
        assertEquals("Hora inválida (formato HH:mm, ej: 15:30)", FormularioValidator.seguimiento(c)["horaAccion"])
    }

    // ---------- login ----------
    @Test
    fun login_datosCorrectos_noTieneErrores() {
        assertTrue(FormularioValidator.login(" supervisor@ficticio.cl ", "duoc123").isEmpty())
    }

    @Test
    fun login_vacio_marcaCorreoYClave() {
        val errores = FormularioValidator.login("", "")
        assertEquals("El correo es obligatorio", errores["correo"])
        assertEquals("Ingrese la contraseña", errores["clave"])
    }

    @Test
    fun login_correoMalFormado_retornaError() {
        assertEquals(setOf("correo"), FormularioValidator.login("sin-arroba", "x").keys)
    }

    // ---------- resumen (aviso al usuario) ----------
    @Test
    fun resumen_sinErrores_retornaNull() {
        assertNull(FormularioValidator.resumen(emptyMap()))
    }

    @Test
    fun resumen_unCampo() {
        assertEquals("Revise el campo: Nombre", FormularioValidator.resumen(mapOf("nombre" to "x")))
    }

    @Test
    fun resumen_dosYTresCampos() {
        assertEquals(
            "Revise 2 campos: Nombre y Región",
            FormularioValidator.resumen(mapOf("nombre" to "x", "region" to "x"))
        )
        assertEquals(
            "Revise 3 campos: Nombre, Región y Horario",
            FormularioValidator.resumen(mapOf("nombre" to "x", "region" to "x", "horario" to "x"))
        )
    }

    @Test
    fun resumen_masDeTres_indicaCuantosFaltan() {
        val e = mapOf("nombre" to "x", "region" to "x", "horario" to "x", "comuna" to "x", "tipo" to "x")
        assertEquals(
            "Revise 5 campos: Nombre, Región, Horario y 2 más",
            FormularioValidator.resumen(e)
        )
    }
}
