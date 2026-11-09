# Explicación del proyecto — Cómo cumple la CheckList y la rúbrica

## 1. Usuarios y permisos
- **2 roles** (`Rol` en `data/model/Enums.kt`): EJECUTIVO y SUPERVISOR.
- **4 cuentas ficticias** precargadas en `data/local/SeedData.kt`: 1 supervisor
  (supervisor@ficticio.cl) y 3 ejecutivos (ejecutivo1..3@ficticio.cl), clave `duoc123`.
- **Inicio y cierre de sesión**: `AuthViewModel` + `SessionManager` (SharedPreferences).
  El inicio de la app salta al Dashboard si ya hay sesión (`MainActivity`).
- **Pantallas según rol**: el dashboard muestra "Panel supervisor" solo a SUPERVISOR;
  `SupervisorScreen` bloquea el acceso a otros roles.
- **Restricciones**: `ReglasEstado.transicionesPermitidas()` (Enums.kt) permite solo
  estados NO finales al ejecutivo; confirmar/descartar es exclusivo del supervisor y
  se verifica también en el repositorio (`cambiarEstado`).
- **Asignar/reasignar**: `AppViewModel.asignar()` (panel supervisor).

## 2. Pantallas y funcionalidades (9 pantallas conectadas por NavGraph)
Login, Dashboard, Oportunidades (lista+búsqueda+filtros), Formulario (crear/editar),
Detalle, Organizaciones+Contactos, Seguimiento, Evidencias, Panel Supervisor.
- Detalle con información completa, chips de estado/prioridad, transiciones permitidas,
  registro de resultado de activación.
- Formulario valida y registra fecha, ubicación, tipo de evento, público esperado y prioridad.
- Alertas mostradas en el Dashboard con navegación al detalle.

## 3. Base de datos y arquitectura
- **Room/SQLite** con 6 entidades: Usuario, Organización, Contacto, Oportunidad,
  Seguimiento, EvidenciaFoto (3FN: cada tabla tiene PK, FK e índices — ver `Entities.kt`).
- **Relaciones**: Oportunidad→Organización y Oportunidad→Usuario (FK restrictivas);
  Seguimiento→Oportunidad (CASCADE); Contacto→Organización (CASCADE).
- **CRUD real**: inserción y actualización reales vía DAO (persistencia al cerrar la app).
- **MVVM**: UI → `AppViewModel`/`AuthViewModel` (StateFlow) → `OportunidadRepository` → DAO.
- **Historial preservado**: los seguimientos nunca se borran.

## 4. Formularios y validaciones
- `domain/validation/Validators.kt`: campos obligatorios, correo, teléfono chileno
  ficticio, fechas yyyy-MM-dd, horario HH:mm - HH:mm, costos y cantidades no negativos,
  potencial 1–5, coherencia de fechas (término ≥ inicio; inicio ≥ detección).
- Errores mostrados con `supportingText`, ícono de error y `isError` en `CampoTexto`.
- La lógica vive en el ViewModel/Validators, NO en los Composables.
- Los valores ingresados se mantienen tras un fallo (el estado vive en `remember`).
- Banner de éxito/error (`BannerResultado`) con descarte.

## 5. Recursos nativos
- **Cámara**: `EvidenciasScreen` + `CameraUtils` (TakePicture + FileProvider),
  permisos en tiempo de ejecución, vista previa de la última captura (Coil),
  conservación del archivo y asociación a la oportunidad en Room.
- **Calendario**: `CalendarUtils.agregarEventoCalendario` usa `ACTION_INSERT`
  (sin permisos de escritura) con DTSTART/DTEND del evento registrado.

## 6. UX/UI y animaciones
- Jetpack Compose + **Material 3**; colores y tipografía centralizados en `ui/theme`
  (`Color.kt`, `Type.kt`, `Theme.kt`), sin hexadecimales sueltos en pantallas.
- **Colores semánticos**: `colorEstado()` y `colorPrioridad()` en Color.kt
  (éxito, advertencia, error, info por estado).
- **Animaciones (≥3 interacciones, 2 mecanismos)**:
  1. `AnimatedVisibility` — panel de filtros y expansión de contactos.
  2. `animateFloatAsState` + spring — logo del login.
  3. `Crossfade` — carga del dashboard.
  4. `slideIn/slideOut + fade` — banner de resultados.
- Estados vacíos (`EstadoVacio`), indicador de carga (`Cargando`), confirmaciones,
  layouts con `verticalScroll` para pantallas pequeñas.
- **Bonus**: ícono vectorial personalizado `@drawable/ic_launcher`.

## 7. Entrega
- Proyecto Android Studio ejecutable, README con arquitectura e instrucciones,
  datos ficticios exclusivamente, defensa preparada (cada pantalla se explica arriba).
