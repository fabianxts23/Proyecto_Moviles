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

## Historias de usuario corregidas (las que no estaban "Aprobado")

### HU-04 · Registrar oportunidad
- **Público esperado:** ahora es la cantidad de personas (número, no negativo). El texto libre pasó a llamarse "Perfil del público esperado" y se actualizó el Detalle.
- **Costos:** se puede escribir un valor negativo y la validación lo informa (antes el campo lo borraba en silencio). Los montos grandes ya no se muestran como `1.5E7` al editar.
- **Gestión:** nueva sección "Gestión" con prioridad, potencial comercial (1-5), estado y responsable (queda asignada a quien la crea).
- Tipo, fuente y organización parten sin seleccionar y son obligatorios; el error aparece bajo cada campo.
- Fechas: el inicio no puede ser anterior a la fecha de detección real (antes se comparaba contra "hace un año") y el término no puede ser anterior al inicio.
- Aviso fijo arriba ("Revise los N campos marcados en rojo") y los errores de cada campo ya no desaparecen a los 3 segundos.
- En edición, el botón espera a que carguen los datos (evita crear un duplicado) y no pisa lo que el usuario escribe.

### HU-06 · Buscar y filtrar
- Nuevo filtro por **organización**; comuna y región filtran por coincidencia parcial (sin distinguir mayúsculas) y se muestra el total de resultados.

### HU-08 · Cambiar estado
- La regla vive en `ReglasEstado.validar()` y se aplica también en el repositorio: cerrada = sin cambios, y los estados finales solo los fija el supervisor, con mensaje claro.
- El Detalle informa cuando una oportunidad está cerrada y cuando el ejecutivo no puede fijar estados finales.
- Al pasar a "Propuesta enviada" se guarda la fecha de la propuesta.

### HU-09 · Asignar / reasignar responsable (supervisor)
- El repositorio valida rol, que la oportunidad esté activa y que el ejecutivo exista (antes un no supervisor fallaba en silencio).
- El responsable actual se muestra siempre (antes aparecía "Sin asignar" si no era ejecutivo) y se marca como "(actual)" en la lista.
- El aviso de resultado ahora está fijo arriba: antes quedaba fuera de pantalla al desplazar la lista.

### HU-10 · Cambiar prioridad (supervisor)
- Botón propio con la prioridad actual, las 4 opciones, validación de rol y de oportunidad activa, y aviso visible. El indicador "Críticas/Alta" se actualiza al instante.

### HU-12 · Organizaciones y contactos
- Los selectores de Tipo de organización y Área mostraban el nombre interno (`INSTITUCION_PRIVADA`); ahora muestran la etiqueta legible.
- Medio preferente pasó a ser una selección (Correo, Teléfono, WhatsApp, Reunión presencial) y se muestra en cada contacto.
- Teléfono chileno más estricto (`+56 9 XXXX XXXX`).

### HU-13 · Seguimiento
- El **tipo de acción** ahora se elige con chips (todas las opciones visibles, con su nombre legible).
- Se agregaron **fecha y hora** de la acción, con validación; la próxima acción no puede ser anterior a esa fecha.
- La próxima acción vence al terminar el día indicado (antes quedaba "atrasada" desde las 00:00).
- Errores bajo cada campo y aviso fijo.

### HU-14 · Alertas y recordatorios
- El Dashboard lista **todas** las alertas (antes solo 3 por tipo), cada una con ícono, color y detalle ("Inicia mañana", "Enviada hace 9 días", etc.).
- El ejecutivo ve solo las alertas de sus oportunidades; el supervisor, las del equipo.
- "Propuesta sin respuesta" cuenta desde la fecha de la propuesta (antes cualquier edición, como cambiar la prioridad, reiniciaba el plazo).

### Otros
- Calendario: se restauraron el título y la descripción del evento.

## Revisión de integración con el backend (Spring Boot)
- **No compilaba:** `SupervisorScreen.kt` tenía una pantalla `ClientesScreen` pegada dentro de `TarjetaIndicador` (llaves sin cerrar, `fillMaxSize(` sin cerrar, `else` suelto y una función `Modifier.background` vacía). Reemplazada por `SeccionUsuariosServidor`.
- **No sincronizaba Gradle:** faltaba el `build.gradle` de la raíz (los plugins no tenían versión) — estaba solo en la carpeta duplicada `OportunidadesApp/`, que se eliminó (era una copia antigua).
- **Dependencias en conflicto:** `lifecycle-runtime-compose` 2.8.3 / 2.9.2 / 2.11.0 a la vez (2.9+ exige compileSdk 35). Unificado en 2.8.3; Retrofit 2.11.0 + OkHttp 4.12.0.
- **Manifest:** `networkSecurityConfig` estaba en `<uses-permission>` (se ignoraba, HTTP bloqueado) → movido a `<application>`; permiso INTERNET declarado correctamente.
- **URL incorrecta:** Retrofit llamaba a `usuarios` y el backend expone `/v1/api/usuarios`.
- **Respuesta:** se usaba `UsuarioEntity` (de Room) como respuesta de red; ahora `UsuarioDto` (sin contraseña).
- **Login conectado:** `AuthViewModel` consulta al backend; si responde OK sincroniza el usuario en Room (por correo, conservando el id local); si rechaza, error; si no hay conexión, usa el login local.
- El panel de supervisor muestra los usuarios del servidor, con carga, error y "Reintentar".
