package cl.duoc.oportunidades.data.local

import androidx.room.withTransaction
import cl.duoc.oportunidades.data.model.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar

/**
 * Precarga de datos 100% ficticios (la contraparte real no se incorpora).
 * Se ejecuta solo si la base está vacía y dentro de una transacción, así
 * nunca queda a medias aunque la app se cierre durante la carga.
 */
object SeedData {

    private val mutex = Mutex()

    /** Inserta los datos iniciales si aún no existen (seguro ante llamadas concurrentes). */
    suspend fun asegurar(db: AppDatabase) = mutex.withLock {
        if (db.usuarioDao().contar() == 0) {
            db.withTransaction { sembrar(db) }
        }
    }

    fun diaEn(nDias: Int, hora: Int = 9): Long =
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, nDias)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private suspend fun sembrar(db: AppDatabase) {
        val uDao = db.usuarioDao()
        val oDao = db.organizacionDao()
        val cDao = db.contactoDao()
        val opDao = db.oportunidadDao()
        val sDao = db.seguimientoDao()

        // ----- Usuarios ficticios: 1 supervisor + 3 ejecutivos -----
        uDao.insertar(
            UsuarioEntity(
                nombre = "Sofía Herrera",
                correo = "supervisor@ficticio.cl",
                password = "duoc123",
                rol = Rol.SUPERVISOR,
                area = "Convenios Corporativos",
                zona = "Santiago"
            )
        )
        val ejecutivos = listOf(
            Triple("Javier Muñoz", "ejecutivo1@ficticio.cl", "Santiago"),
            Triple("Carla Rojas", "ejecutivo2@ficticio.cl", "Concepción"),
            Triple("Andrés Paredes", "ejecutivo3@ficticio.cl", "Puente Alto")
        ).map { (n, c, z) ->
            val id = uDao.insertar(
                UsuarioEntity(nombre = n, correo = c, password = "duoc123",
                    rol = Rol.EJECUTIVO, area = "Ventas", zona = z)
            )
            uDao.porId(id)!!
        }

        // ----- Organizaciones ficticias -----
        val orgIds = listOf(
            OrganizacionEntity(nombre = "Municipalidad de San Ficticio",
                tipo = TipoOrganizacion.MUNICIPALIDAD, rubro = "Municipal",
                comuna = "Puente Alto", region = "Metropolitana",
                sitioWeb = "https://sanficticio-ejemplo.cl",
                publicoAsociado = "Vecinos y trabajadores municipales",
                observaciones = "Feria anual de beneficios para funcionarios."),
            OrganizacionEntity(nombre = "Empresa Andina SpA",
                tipo = TipoOrganizacion.INSTITUCION_PRIVADA, rubro = "Logística",
                comuna = "Providencia", region = "Metropolitana",
                sitioWeb = "https://andina-ejemplo.cl",
                publicoAsociado = "Colaboradores de planta y oficina",
                observaciones = "Jornadas de bienestar semestrales."),
            OrganizacionEntity(nombre = "Corporación Boreal",
                tipo = TipoOrganizacion.INSTITUCION_PUBLICA, rubro = "Educación",
                comuna = "Concepción", region = "Biobío",
                sitioWeb = "https://boreal-ejemplo.cl",
                publicoAsociado = "Académicos y administrativos",
                observaciones = "Interés en convenio de salud dental."),
            OrganizacionEntity(nombre = "Centro Comercial Los Robles",
                tipo = TipoOrganizacion.CENTRO_COMERCIAL, rubro = "Retail",
                comuna = "Temuco", region = "La Araucanía",
                sitioWeb = "https://losrobles-ejemplo.cl",
                publicoAsociado = "Visitantes y trabajadores del mall",
                observaciones = "Espacios para activaciones comerciales."),
            OrganizacionEntity(nombre = "Asociación Gremial del Sur",
                tipo = TipoOrganizacion.ASOCIACION, rubro = "Gremial",
                comuna = "Valdivia", region = "Los Ríos",
                sitioWeb = "https://gremialsur-ejemplo.cl",
                publicoAsociado = "Socios gremiales y familias",
                observaciones = "Eventos anuales con alta afluencia.")
        ).map { oDao.insertar(it) }

        // ----- Contactos corporativos ficticios -----
        val contactos = listOf(
            ContactoEntity(organizacionId = orgIds[0], nombre = "Marcela Fuentes",
                cargo = "Coordinadora", area = AreaContacto.EVENTOS,
                correo = "m.fuentes@sanficticio-ejemplo.cl", telefono = "+56 9 5555 0101",
                medioPreferente = "Correo", estado = "Activo",
                fechaUltimoContacto = diaEn(-4)),
            ContactoEntity(organizacionId = orgIds[1], nombre = "Rodrigo Campos",
                cargo = "Jefe de Bienestar", area = AreaContacto.BIENESTAR,
                correo = "r.campos@andina-ejemplo.cl", telefono = "+56 9 5555 0202",
                medioPreferente = "Teléfono", estado = "Activo"),
            ContactoEntity(organizacionId = orgIds[2], nombre = "Paula Soto",
                cargo = "Analista RRHH", area = AreaContacto.RECURSOS_HUMANOS,
                correo = "p.soto@boreal-ejemplo.cl", telefono = "+56 9 5555 0303",
                medioPreferente = "Correo", estado = "Activo"),
            ContactoEntity(organizacionId = orgIds[3], nombre = "Ignacio Vera",
                cargo = "Gerente de Marketing", area = AreaContacto.MARKETING,
                correo = "i.vera@losrobles-ejemplo.cl", telefono = "+56 9 5555 0404",
                medioPreferente = "Correo", estado = "Activo")
        ).map { cDao.insertar(it) }

        // ----- Oportunidades ficticias en distintos estados -----
        val op1 = opDao.insertar(OportunidadEntity(
            nombre = "Feria de Beneficios Municipal 2026",
            tipo = TipoOportunidad.FERIA_INSTITUCIONAL,
            descripcion = "Feria anual de beneficios para funcionarios municipales.",
            fuente = FuenteInformacion.SITIO_WEB,
            fechaDeteccion = diaEn(-15),
            eventoNombre = "Feria de Beneficios San Ficticio",
            eventoTipo = "Feria institucional",
            fechaInicio = diaEn(5), fechaTermino = diaEn(6, 18), horario = "10:00 - 18:00",
            direccion = "Plaza de Armas (ficticia)", comuna = "Puente Alto",
            region = "Metropolitana", organizacionId = orgIds[0],
            publicoEsperado = "Trabajadores municipales y familias",
            publicoObjetivo = "Trabajadores con cobertura de salud",
            cantidadAsistentesEstimada = 800,
            condiciones = "Stand de 3x3 incluido", requisitos = "Presentación de propuesta",
            costosReferenciales = 250000.0, espaciosDisponibles = "10 stands",
            prioridad = Prioridad.ALTA, potencialComercial = 4,
            estado = EstadoOportunidad.PROPUESTA_ENVIADA, responsableId = ejecutivos[0].id,
            propuestaEnviada = true, fechaPropuesta = diaEn(-9),
            fechaUltimaActualizacion = diaEn(-9)
        ))

        val op2 = opDao.insertar(OportunidadEntity(
            nombre = "Jornada de Bienestar Andina",
            tipo = TipoOportunidad.JORNADA_BENEFICIOS,
            descripcion = "Jornada de bienestar para colaboradores de Empresa Andina.",
            fuente = FuenteInformacion.CONTACTO_DIRECTO,
            fechaDeteccion = diaEn(-8),
            eventoNombre = "Bienestar Andina 2026",
            eventoTipo = "Jornada de beneficios",
            fechaInicio = diaEn(12), fechaTermino = diaEn(12, 17), horario = "09:00 - 17:00",
            direccion = "Casa matriz (ficticia)", comuna = "Providencia",
            region = "Metropolitana", organizacionId = orgIds[1],
            publicoEsperado = "Colaboradores de oficina",
            publicoObjetivo = "Colaboradores 25-50 años",
            cantidadAsistentesEstimada = 350,
            condiciones = "Stand en patio central", requisitos = "Confirmación por correo",
            costosReferenciales = 120000.0, espaciosDisponibles = "4 espacios",
            prioridad = Prioridad.MEDIA, potencialComercial = 3,
            estado = EstadoOportunidad.CONTACTO_REALIZADO, responsableId = ejecutivos[1].id
        ))

        val op3 = opDao.insertar(OportunidadEntity(
            nombre = "Actividad Anual Gremial del Sur",
            tipo = TipoOportunidad.ACTIVIDAD_TRABAJADORES,
            descripcion = "Actividad de fin de año para socios y familias.",
            fuente = FuenteInformacion.REDES_SOCIALES,
            fechaDeteccion = diaEn(-3),
            eventoNombre = "Cierre de Año Gremial",
            eventoTipo = "Actividad de trabajadores",
            fechaInicio = diaEn(30), fechaTermino = diaEn(30, 22), horario = "15:00 - 22:00",
            direccion = "Centro de eventos (ficticio)", comuna = "Valdivia",
            region = "Los Ríos", organizacionId = orgIds[4],
            publicoEsperado = "Socios y familias", publicoObjetivo = "Familias",
            cantidadAsistentesEstimada = 500,
            condiciones = "Por definir", requisitos = "Por definir",
            costosReferenciales = 0.0, espaciosDisponibles = "Por definir",
            prioridad = Prioridad.BAJA, potencialComercial = 2,
            estado = EstadoOportunidad.EVENTO_IDENTIFICADO, responsableId = ejecutivos[2].id
        ))

        val op4 = opDao.insertar(OportunidadEntity(
            nombre = "Activación Los Robles Primavera",
            tipo = TipoOportunidad.ACTIVACION_COMERCIAL,
            descripcion = "Activación comercial en centro comercial.",
            fuente = FuenteInformacion.REFERENCIA,
            fechaDeteccion = diaEn(-40),
            eventoNombre = "Primavera en Los Robles",
            eventoTipo = "Activación comercial",
            fechaInicio = diaEn(-10), fechaTermino = diaEn(-10, 20), horario = "11:00 - 20:00",
            direccion = "Patio central (ficticio)", comuna = "Temuco",
            region = "La Araucanía", organizacionId = orgIds[3],
            publicoEsperado = "Visitantes del mall", publicoObjetivo = "Público general",
            cantidadAsistentesEstimada = 1200,
            condiciones = "Arriendo de espacio", requisitos = "Contrato de arriendo ficticio",
            costosReferenciales = 400000.0, espaciosDisponibles = "1 patio central",
            prioridad = Prioridad.CRITICA, potencialComercial = 5,
            estado = EstadoOportunidad.ACTIVACION_REALIZADA, responsableId = ejecutivos[0].id,
            resEstadoFinal = "Exitosa", resFechaRealizacion = diaEn(-10),
            resAsistentes = 1100, resContactosGenerados = 240,
            resPersonasInteresadas = 95, resInscripciones = 42,
            resObservaciones = "Alta afluencia en horario de tarde.",
            resEvaluacion = 5, resAprendizajes = "Contratar segundo ejecutivo de stand."
        ))

        // ----- Seguimientos ficticios -----
        sDao.insertar(SeguimientoEntity(
            oportunidadId = op1, usuarioId = ejecutivos[0].id,
            tipoAccion = TipoAccion.PRIMER_CONTACTO, fechaHora = diaEn(-12),
            resultado = "Se coordinó llamada con coordinadora de eventos.",
            proximaAccion = "Enviar propuesta formal", fechaProximo = diaEn(-9)))
        sDao.insertar(SeguimientoEntity(
            oportunidadId = op1, usuarioId = ejecutivos[0].id,
            tipoAccion = TipoAccion.ENVIO_PROPUESTA, fechaHora = diaEn(-9),
            resultado = "Propuesta enviada al correo ficticio de la organización.",
            proximaAccion = "Esperar respuesta y hacer seguimiento", fechaProximo = diaEn(3)))
        sDao.insertar(SeguimientoEntity(
            oportunidadId = op2, usuarioId = ejecutivos[1].id,
            tipoAccion = TipoAccion.REUNION, fechaHora = diaEn(-5),
            resultado = "Reunión con jefe de bienestar, interés confirmado.",
            proximaAccion = "Solicitar antecedentes formales", fechaProximo = diaEn(-1)))
    }
}
