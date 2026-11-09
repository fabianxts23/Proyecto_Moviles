package cl.duoc.oportunidades.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password AND cuentaActiva = 1")
    suspend fun login(correo: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun porId(id: Long): UsuarioEntity?

    /** rol = 0 corresponde a Rol.EJECUTIVO (ordinal). */
    @Query("SELECT * FROM usuarios WHERE rol = 0 ORDER BY nombre")
    suspend fun listarEjecutivos(): List<UsuarioEntity>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(u: UsuarioEntity): Long
}

@Dao
interface OrganizacionDao {
    @Query("SELECT * FROM organizaciones ORDER BY nombre")
    fun observarTodas(): Flow<List<OrganizacionEntity>>

    @Query("SELECT * FROM organizaciones WHERE id = :id")
    fun observarPorId(id: Long): Flow<OrganizacionEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(org: OrganizacionEntity): Long
}

@Dao
interface ContactoDao {
    @Query("SELECT * FROM contactos WHERE organizacionId = :orgId ORDER BY nombre")
    fun observarPorOrganizacion(orgId: Long): Flow<List<ContactoEntity>>

    @Query("SELECT * FROM contactos WHERE id = :id")
    suspend fun porId(id: Long): ContactoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(c: ContactoEntity): Long

    @Query("UPDATE contactos SET fechaUltimoContacto = :fecha WHERE id = :id")
    suspend fun actualizarFechaContacto(id: Long, fecha: Long)
}

@Dao
interface OportunidadDao {
    @Query("SELECT * FROM oportunidades ORDER BY prioridad DESC, fechaInicio ASC")
    fun observarTodas(): Flow<List<OportunidadEntity>>

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    fun observarPorId(id: Long): Flow<OportunidadEntity?>

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    suspend fun porId(id: Long): OportunidadEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(o: OportunidadEntity): Long

    @Update
    suspend fun actualizar(o: OportunidadEntity)

    // ---------- Alertas / recordatorios (punto 3.2 Información de alertas) ----------

    /** Eventos próximos: inician dentro de los próximos N días y no están cerrados (estados finales = ordinales 8..11). */
    @Query("""SELECT * FROM oportunidades
              WHERE fechaInicio >= :hoy AND fechaInicio <= :limite
              AND estado NOT IN (8, 9, 10, 11)
              ORDER BY fechaInicio ASC""")
    fun observarEventosProximos(hoy: Long, limite: Long): Flow<List<OportunidadEntity>>

    /** Oportunidades sin contacto: EVENTO_IDENTIFICADO (0) o CONTACTO_PENDIENTE (1). */
    @Query("""SELECT * FROM oportunidades
              WHERE estado IN (0, 1)
              ORDER BY fechaDeteccion ASC""")
    fun observarSinContacto(): Flow<List<OportunidadEntity>>

    /** Propuestas enviadas (PROPUESTA_ENVIADA = 3) sin respuesta hace más de N días. */
    @Query("""SELECT * FROM oportunidades
              WHERE estado = 3 AND fechaUltimaActualizacion < :desde
              ORDER BY fechaUltimaActualizacion ASC""")
    fun observarPropuestasSinRespuesta(desde: Long): Flow<List<OportunidadEntity>>

    /** Oportunidades activas que no tienen ninguna acción de seguimiento registrada. */
    @Query("""SELECT o.* FROM oportunidades o
              WHERE o.estado NOT IN (8, 9, 10, 11)
              AND NOT EXISTS (SELECT 1 FROM seguimientos s WHERE s.oportunidadId = o.id)
              ORDER BY o.fechaDeteccion ASC""")
    fun observarSinSeguimiento(): Flow<List<OportunidadEntity>>

    /**
     * Gestiones atrasadas: la última gestión registrada tiene una próxima acción
     * cuya fecha ya venció (las gestiones antiguas ya superadas no cuentan).
     */
    @Query("""SELECT o.* FROM oportunidades o
              WHERE o.estado NOT IN (8, 9, 10, 11)
              AND EXISTS (
                  SELECT 1 FROM seguimientos s
                  WHERE s.oportunidadId = o.id
                  AND s.fechaProximo < :ahora
                  AND s.fechaHora = (SELECT MAX(s2.fechaHora) FROM seguimientos s2
                                     WHERE s2.oportunidadId = o.id)
              )
              ORDER BY o.fechaInicio ASC""")
    fun observarGestionesAtrasadas(ahora: Long): Flow<List<OportunidadEntity>>
}

@Dao
interface SeguimientoDao {
    @Query("SELECT * FROM seguimientos WHERE oportunidadId = :id ORDER BY fechaHora DESC")
    fun observarPorOportunidad(id: Long): Flow<List<SeguimientoEntity>>

    @Query("SELECT * FROM seguimientos WHERE oportunidadId = :id ORDER BY fechaHora DESC")
    suspend fun listarPorOportunidad(id: Long): List<SeguimientoEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(s: SeguimientoEntity): Long
}

@Dao
interface EvidenciaFotoDao {
    @Query("SELECT * FROM evidencias_foto WHERE oportunidadId = :id ORDER BY fecha DESC")
    fun observarPorOportunidad(id: Long): Flow<List<EvidenciaFotoEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(e: EvidenciaFotoEntity): Long

    @Query("DELETE FROM evidencias_foto WHERE id = :id")
    suspend fun eliminar(id: Long)
}
