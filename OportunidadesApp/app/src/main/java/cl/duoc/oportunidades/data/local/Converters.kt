package cl.duoc.oportunidades.data.local

import androidx.room.TypeConverter
import cl.duoc.oportunidades.data.model.*

class Converters {
    @TypeConverter fun rolToInt(v: Rol) = v.ordinal
    @TypeConverter fun intToRol(v: Int) = Rol.values()[v]

    @TypeConverter fun tipoOportunidadToInt(v: TipoOportunidad) = v.ordinal
    @TypeConverter fun intToTipoOportunidad(v: Int) = TipoOportunidad.values()[v]

    @TypeConverter fun prioridadToInt(v: Prioridad) = v.ordinal
    @TypeConverter fun intToPrioridad(v: Int) = Prioridad.values()[v]

    @TypeConverter fun estadoToInt(v: EstadoOportunidad) = v.ordinal
    @TypeConverter fun intToEstado(v: Int) = EstadoOportunidad.values()[v]

    @TypeConverter fun tipoOrganizacionToInt(v: TipoOrganizacion) = v.ordinal
    @TypeConverter fun intToTipoOrganizacion(v: Int) = TipoOrganizacion.values()[v]

    @TypeConverter fun areaToInt(v: AreaContacto) = v.ordinal
    @TypeConverter fun intToArea(v: Int) = AreaContacto.values()[v]

    @TypeConverter fun tipoAccionToInt(v: TipoAccion) = v.ordinal
    @TypeConverter fun intToTipoAccion(v: Int) = TipoAccion.values()[v]

    @TypeConverter fun fuenteToInt(v: FuenteInformacion) = v.ordinal
    @TypeConverter fun intToFuente(v: Int) = FuenteInformacion.values()[v]
}
