package cl.duoc.oportunidades.camera

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Utilidad para capturar fotografías con la cámara del dispositivo.
 * Crea un archivo temporal y lo expone mediante FileProvider para
 * que la cámara del sistema pueda escribir en él.
 */
object CameraUtils {

    const val AUTHORITY = "cl.duoc.oportunidades.fileprovider"

    /** Crea el Uri destino de la próxima fotografía. */
    fun crearUriFoto(context: Context): Uri {
        val dir = File(context.cacheDir, "fotos").apply { mkdirs() }
        val archivo = File(dir, "evidencia_${UUID.randomUUID()}.jpg").apply { createNewFile() }
        return FileProvider.getUriForFile(context, AUTHORITY, archivo)
    }

    /** Copia la foto del cache a almacenamiento propio de la app para conservarla. */
    fun conservarFoto(context: Context, uriCache: Uri): Uri {
        val dir = File(context.filesDir, "evidencias").apply { mkdirs() }
        val destino = File(dir, "foto_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uriCache)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        return Uri.fromFile(destino)
    }
}
