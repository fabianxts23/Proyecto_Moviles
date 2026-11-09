package cl.duoc.oportunidades.calendar

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast

/**
 * Integración con el calendario del dispositivo.
 * Usa ACTION_INSERT (no requiere permisos de escritura): el usuario
 * confirma el evento en la app de calendario. Las fechas/horas
 * entregadas se usan tal cual en el evento.
 */
object CalendarUtils {

    fun agregarEventoCalendario(
        context: Context,
        titulo: String,
        ubicacion: String,
        inicioMillis: Long,
        finMillis: Long,
        descripcion: String
    ) {
        // Si inicio y término coinciden (fechas sin hora), se asignan 2 horas.
        val fin = if (finMillis <= inicioMillis) inicioMillis + 2 * 60 * 60 * 1000L else finMillis
        try {
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, titulo)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, ubicacion)
                .putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fin)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se encontró una app de calendario",
                Toast.LENGTH_LONG).show()
        }
    }
}
