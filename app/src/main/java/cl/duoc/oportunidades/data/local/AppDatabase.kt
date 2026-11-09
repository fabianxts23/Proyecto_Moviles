package cl.duoc.oportunidades.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UsuarioEntity::class,
        OrganizacionEntity::class,
        ContactoEntity::class,
        OportunidadEntity::class,
        SeguimientoEntity::class,
        EvidenciaFotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun contactoDao(): ContactoDao
    abstract fun oportunidadDao(): OportunidadDao
    abstract fun seguimientoDao(): SeguimientoDao
    abstract fun evidenciaFotoDao(): EvidenciaFotoDao

    companion object {
        @Volatile private var INSTANCIA: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oportunidades.db"
                ).build()
                    .also { INSTANCIA = it }
            }
    }
}
