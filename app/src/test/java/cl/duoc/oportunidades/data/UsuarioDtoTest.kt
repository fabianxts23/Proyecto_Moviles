package cl.duoc.oportunidades.data

import cl.duoc.oportunidades.data.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la conversión del usuario del backend a la entidad local. */
class UsuarioDtoTest {

    @Test
    fun aEntidadLocal_normalizaCorreoYGuardaPassword() {
        val dto = UsuarioDto(id = 9, nombre = "Ana", correo = "  ANA@Ejemplo.CL ", rol = Rol.EJECUTIVO,
            area = "Eventos", zona = "Norte", fechaCreacion = 1000L)

        val entidad = dto.aEntidadLocal("clave123")

        assertEquals(0L, entidad.id) // el id local lo asigna Room
        assertEquals("ana@ejemplo.cl", entidad.correo)
        assertEquals("clave123", entidad.password)
        assertEquals("Eventos", entidad.area)
        assertEquals("Norte", entidad.zona)
        assertEquals(1000L, entidad.fechaCreacion)
        assertEquals(Rol.EJECUTIVO, entidad.rol)
    }

    @Test
    fun aEntidadLocal_camposNulos_usanValoresPorDefecto() {
        val antes = System.currentTimeMillis()
        val dto = UsuarioDto(id = 1, nombre = "Luis", correo = "l@x.cl", rol = Rol.SUPERVISOR)

        val entidad = dto.aEntidadLocal("p")

        assertEquals("", entidad.area)
        assertEquals("", entidad.zona)
        assertTrue(entidad.cuentaActiva)
        assertTrue(entidad.fechaCreacion >= antes)
    }
}
