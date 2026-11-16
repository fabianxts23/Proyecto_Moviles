package cl.duoc.oportunidades.data

import cl.duoc.oportunidades.data.local.UsuarioEntity
import cl.duoc.oportunidades.data.model.Rol

/** Usuario tal como lo entrega el backend (nunca incluye la contraseña). */
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val area: String? = null,
    val zona: String? = null,
    val cuentaActiva: Boolean = true,
    val fechaCreacion: Long? = null
) {
    /** Convierte a la entidad local. La contraseña se guarda solo para permitir login sin conexión. */
    fun aEntidadLocal(passwordPlano: String) = UsuarioEntity(
        id = 0,
        nombre = nombre,
        correo = correo.trim().lowercase(),
        password = passwordPlano,
        rol = rol,
        area = area ?: "",
        zona = zona ?: "",
        cuentaActiva = cuentaActiva,
        fechaCreacion = fechaCreacion ?: System.currentTimeMillis()
    )
}

data class LoginRequest(val correo: String, val password: String)
