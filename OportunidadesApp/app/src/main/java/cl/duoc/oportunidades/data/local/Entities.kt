package cl.duoc.oportunidades.data.local

import androidx.room.*
import cl.duoc.oportunidades.data.model.*

/**
 * Modelo de datos relacional (3FN).
 * Cada entidad tiene PK propia y las relaciones se modelan con FK.
 * Los enums se persisten como Int (ordinal) mediante Converters.
 */

@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: Rol,
    val area: String,
    val zona: String,
    val cuentaActiva: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)

@Entity(tableName = "organizaciones")
data class OrganizacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String,
    val comuna: String,
    val region: String,
    val sitioWeb: String,
    val publicoAsociado: String,
    val observaciones: String
)

@Entity(
    tableName = "contactos",
    foreignKeys = [ForeignKey(
        entity = OrganizacionEntity::class,
        parentColumns = ["id"],
        childColumns = ["organizacionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("organizacionId")]
)
data class ContactoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organizacionId: Long,
    val nombre: String,
    val cargo: String,
    val area: AreaContacto,
    val correo: String,
    val telefono: String,
    val medioPreferente: String,
    val estado: String,
    val fechaUltimoContacto: Long? = null
)

@Entity(
    tableName = "oportunidades",
    foreignKeys = [
        ForeignKey(
            entity = OrganizacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["organizacionId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("organizacionId"), Index("responsableId"), Index("estado"), Index("fechaInicio")]
)
data class OportunidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Oportunidad
    val nombre: String,
    val tipo: TipoOportunidad,
    val descripcion: String,
    val fuente: FuenteInformacion,
    val fechaDeteccion: Long,
    // Evento asociado
    val eventoNombre: String,
    val eventoTipo: String,
    val fechaInicio: Long,
    val fechaTermino: Long,
    val horario: String,
    val direccion: String,
    val comuna: String,
    val region: String,
    val organizacionId: Long,
    // Público y evaluación
    val publicoEsperado: String,
    val publicoObjetivo: String,
    val cantidadAsistentesEstimada: Int,
    // Participación
    val condiciones: String,
    val requisitos: String,
    val costosReferenciales: Double,
    val espaciosDisponibles: String,
    // Gestión
    val prioridad: Prioridad,
    val potencialComercial: Int,          // 1..5
    val estado: EstadoOportunidad,
    val responsableId: Long,
    val fechaUltimaActualizacion: Long = System.currentTimeMillis(),
    // Propuesta
    val propuestaEnviada: Boolean = false,
    val fechaPropuesta: Long? = null,
    // Resultado de activación (nulo si aún no se realiza)
    val resEstadoFinal: String? = null,
    val resFechaRealizacion: Long? = null,
    val resAsistentes: Int? = null,
    val resContactosGenerados: Int? = null,
    val resPersonasInteresadas: Int? = null,
    val resInscripciones: Int? = null,
    val resObservaciones: String? = null,
    val resEvaluacion: Int? = null,
    val resAprendizajes: String? = null
)

@Entity(
    tableName = "seguimientos",
    foreignKeys = [
        ForeignKey(
            entity = OportunidadEntity::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("oportunidadId"), Index("usuarioId"), Index("fechaProximo")]
)
data class SeguimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    val usuarioId: Long,
    val tipoAccion: TipoAccion,
    val fechaHora: Long,
    val resultado: String,
    val proximaAccion: String,
    val fechaProximo: Long
)

/** Evidencia fotográfica capturada con la cámara del dispositivo. */
@Entity(
    tableName = "evidencias_foto",
    foreignKeys = [ForeignKey(
        entity = OportunidadEntity::class,
        parentColumns = ["id"],
        childColumns = ["oportunidadId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("oportunidadId")]
)
data class EvidenciaFotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val oportunidadId: Long,
    val uri: String,
    val fecha: Long = System.currentTimeMillis()
)
