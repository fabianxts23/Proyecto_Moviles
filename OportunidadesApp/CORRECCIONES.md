# Correcciones realizadas

## Errores que impedían compilar
- `OportunidadRepository.ejecutivos()` usaba `firstOrNull(flow)` inexistente → consulta `suspend` en el DAO.
- `SeedData` llamaba `UsuarioDao.insertar()` que no existía → agregado.
- `Theme.kt` usaba `Color_Exito` antes de declararlo → color `SuperficieVariante` en `Color.kt`.
- `SeguimientoScreen`: import `viewmodel` (minúscula) y falta el import del ícono `History`.
- `OportunidadDetalleScreen`: llamaba funciones `suspend` desde un lambda normal → `AppViewModel.registrarResultado()`.
- `Card(onClick)` sin `@OptIn(ExperimentalMaterial3Api)` en `TarjetaOportunidad` y `AlertaFila`.
- Comentarios `--` dentro de las consultas Room eliminados.

## Errores que fallaban al ejecutar
- Cada ViewModel tenía su propia `SessionManager` sin usuario → `responsableId = 0` (violación de clave foránea),
  el supervisor no podía confirmar/descartar/asignar. Ahora la sesión se comparte.
- `vm.oportunidad(id)`, `vm.seguimientos(id)`, `vm.fotos(id)`, `vm.contactos(id)` creaban un flujo nuevo en cada
  recomposición (pantallas parpadeando/cargando sin fin) → `remember(id)`.
- La precarga se hacía en un callback asíncrono: el primer login podía fallar con "credenciales incorrectas".
  Ahora es `SeedData.asegurar()`, idempotente, transaccional y el login la espera.
- Contactos: los errores de validación nunca se mostraban en el diálogo.
- Alerta "gestión atrasada" marcaba gestiones ya superadas; ahora usa la última gestión de cada oportunidad.
- El botón "Crear oportunidad" quedaba deshabilitado sin explicación.
- Los banners de éxito/error se borraban al instante; ahora duran 3 segundos.
- Eventos de calendario con duración cero; ahora duran mínimo 2 horas.
- El dashboard del ejecutivo mostraba todas las oportunidades; ahora solo las suyas.
- Diálogos largos ahora hacen scroll en pantallas pequeñas.

## Configuración
- Permisos de calendario e internet innecesarios eliminados del manifiesto.
- Agregados `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.7) y `.gitignore`.
- README: requiere Android Studio Koala (2024.1.1) o superior (por AGP 8.5.2).
