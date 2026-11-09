package cl.duoc.oportunidades.domain.validation

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Lógica de validación separada de los Composables (punto 4 del checklist).
 * Cada función retorna el mensaje de error o null si el valor es válido.
 */
object Validators {

    fun requerido(valor: String?, campo: String = "Este campo"): String? =
        if (valor.isNullOrBlank()) "$campo es obligatorio" else null

    fun email(valor: String): String? {
        if (valor.isBlank()) return "El correo es obligatorio"
        val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return if (!regex.matches(valor)) "Formato de correo inválido (ej: nombre@ejemplo.cl)" else null
    }

    fun telefono(valor: String): String? {
        if (valor.isBlank()) return "El teléfono es obligatorio"
        val regex = Regex("^\\+?56?\\s?9\\s?\\d{4}\\s?\\d{4}$")
        return if (!regex.matches(valor.trim()))
            "Formato inválido (ej: +56 9 5555 0101)" else null
    }

    fun fechaValida(valor: String): String? {
        if (valor.isBlank()) return "La fecha es obligatoria"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            isLenient = false
        }
        return try {
            sdf.parse(valor) ?: return "Fecha inválida (formato yyyy-MM-dd)"
            null
        } catch (e: Exception) {
            "Fecha inválida (formato yyyy-MM-dd)"
        }
    }

    fun horario(valor: String): String? {
        if (valor.isBlank()) return "El horario es obligatorio"
        val regex = Regex("^\\d{2}:\\d{2}\\s*[-–]\\s*\\d{2}:\\d{2}$")
        return if (!regex.matches(valor.trim())) "Formato HH:mm - HH:mm (ej: 10:00 - 18:00)" else null
    }

    fun enteroNoNegativo(valor: String, campo: String): String? {
        if (valor.isBlank()) return "$campo es obligatorio"
        val n = valor.toIntOrNull() ?: return "$campo debe ser numérico"
        return if (n < 0) "$campo no puede ser negativo" else null
    }

    fun costoNoNegativo(valor: String): String? {
        if (valor.isBlank()) return "El costo es obligatorio (use 0 si no aplica)"
        val n = valor.toDoubleOrNull() ?: return "El costo debe ser numérico"
        return if (n < 0) "El costo no puede ser negativo" else null
    }

    /** Reglas de negocio: fechas coherentes entre sí. */
    fun coherenciaFechas(deteccion: Long, inicio: Long, termino: Long): String? {
        if (termino < inicio) return "La fecha de término no puede ser anterior al inicio"
        if (inicio < deteccion) return "El evento no puede iniciar antes de ser detectado"
        return null
    }

    fun potencial(valor: String): String? {
        val n = valor.toIntOrNull() ?: return "Debe ser un número del 1 al 5"
        return if (n !in 1..5) "El potencial debe estar entre 1 y 5" else null
    }

    /** Ejecuta todas las validaciones de un campo y retorna el primer error. */
    fun validar(vararg reglas: String?): String? = reglas.firstOrNull { it != null }
}
