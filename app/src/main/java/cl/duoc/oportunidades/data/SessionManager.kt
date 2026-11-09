package cl.duoc.oportunidades.data

import android.content.Context
import cl.duoc.oportunidades.data.local.UsuarioEntity
import cl.duoc.oportunidades.data.model.Rol

/**
 * Sesión local del usuario autenticado (persiste al cerrar la app).
 * El usuario actual se comparte entre todas las instancias (ViewModels,
 * MainActivity) para que todas vean la misma sesión.
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("sesion", Context.MODE_PRIVATE)

    val usuarioActual: UsuarioEntity?
        get() {
            if (cache == null) restaurarSesion()
            return cache
        }

    val estaAutenticado: Boolean get() = usuarioActual != null
    val rol: Rol? get() = usuarioActual?.rol

    fun guardarSesion(usuario: UsuarioEntity) {
        cache = usuario
        prefs.edit()
            .putLong("uid", usuario.id)
            .putString("nombre", usuario.nombre)
            .putString("correo", usuario.correo)
            .putInt("rol", usuario.rol.ordinal)
            .apply()
    }

    /** Reconstruye el usuario desde SharedPreferences. Retorna true si había sesión. */
    fun restaurarSesion(): Boolean {
        if (cache != null) return true
        val uid = prefs.getLong("uid", -1L)
        if (uid == -1L) return false
        val nombre = prefs.getString("nombre", null) ?: return false
        val correo = prefs.getString("correo", null) ?: return false
        val rolIdx = prefs.getInt("rol", 0)
        val rol = Rol.values().getOrNull(rolIdx) ?: return false
        cache = UsuarioEntity(
            id = uid, nombre = nombre, correo = correo,
            password = "", rol = rol, area = "", zona = ""
        )
        return true
    }

    fun cerrarSesion() {
        cache = null
        prefs.edit().clear().apply()
    }

    private companion object {
        @Volatile private var cache: UsuarioEntity? = null
    }
}
