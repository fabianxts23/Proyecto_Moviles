package cl.duoc.oportunidades.data

/**
 * Regla de sincronización de organizaciones backend -> app.
 * Los ids de MySQL y los de Room son distintos, así que se identifican por nombre
 * (sin distinguir mayúsculas ni espacios en los extremos). Sin dependencias de Android.
 */
object OrganizacionSync {

    fun clave(nombre: String): String = nombre.trim().lowercase()

    /**
     * De las organizaciones del backend, devuelve solo las que la app aún no tiene
     * (y sin repetir entre sí), para no duplicar datos ni romper las oportunidades existentes.
     */
    fun <T> nuevas(
        remotas: List<T>,
        nombreDe: (T) -> String,
        nombresLocales: Collection<String>
    ): List<T> {
        val vistos = nombresLocales.map { clave(it) }.toMutableSet()
        return remotas.filter { r ->
            val k = clave(nombreDe(r))
            k.isNotEmpty() && vistos.add(k)
        }
    }
}
