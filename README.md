# Oportunidades Corporativas — DSY1105

Aplicación móvil Android (Kotlin + Jetpack Compose) para la gestión de
oportunidades y activaciones corporativas del caso DSY1105.
**Todos los datos son 100% ficticios.**

## Integrantes
- Integrante 1 — <correo@duoc.cl>
- Integrante 2 — <correo@duoc.cl>

## Funcionalidades
- Autenticación con roles (Ejecutivo / Supervisor) y sesión persistente.
- CRUD de oportunidades asociadas a eventos, organizaciones y contactos ficticios.
- Filtros por estado, prioridad, tipo, comuna, región y organización.
- Alertas: eventos próximos, sin contacto, propuestas sin respuesta, gestiones atrasadas.
- Seguimiento con historial completo y próximas acciones.
- Resultado de activación (asistentes, contactos, inscripciones, evaluación).
- Evidencias fotográficas con cámara del dispositivo.
- Integración con calendario del dispositivo.
- Panel de supervisor: asignación de ejecutivos, prioridad, confirmar/descartar.

## Arquitectura
MVVM: UI (Compose) → ViewModel (StateFlow) → Repository → Room (SQLite) y API REST (Retrofit → backend Spring Boot).
El login consulta primero al backend (`POST /v1/api/usuarios/login`); si no hay conexión, usa la base local.
Modelo de datos en 3FN con claves primarias, foráneas e índices.

## Cuentas de prueba
| Rol | Correo | Clave |
|---|---|---|
| Supervisor | supervisor@ficticio.cl | duoc123 |
| Ejecutivo | ejecutivo1@ficticio.cl | duoc123 |
| Ejecutivo | ejecutivo2@ficticio.cl | duoc123 |
| Ejecutivo | ejecutivo3@ficticio.cl | duoc123 |

## Instrucciones de ejecución
1. Abrir la carpeta en Android Studio **Koala (2024.1.1) o superior** con JDK 17 (incluido en Android Studio). Los módulos usan Kotlin DSL (`*.gradle.kts`) y catálogo de versiones (`gradle/libs.versions.toml`).
2. Esperar la sincronización de Gradle (descarga Gradle 8.13 y genera `local.properties`; requiere internet la primera vez).
3. Ejecutar en emulador o dispositivo con API 26+ (recomendado API 34).
4. La base de datos se precarga sola con datos ficticios en el primer arranque.

## Estructura
Misma organización que el proyecto de referencia *consumiendo-api-usuarios* (capas `data` y `ui`):
```
app/src/main/java/cl/duoc/oportunidades/
├── MainActivity.kt
├── data/
│   ├── UsuariosApi.kt          (interfaz Retrofit)
│   ├── RetrofitClient.kt       (instancia única de Retrofit)
│   ├── UsuarioDto.kt           (modelo que entrega la API)
│   ├── UsuarioRepository.kt    (acceso a la API)
│   ├── OportunidadRepository.kt, SessionManager.kt
│   ├── model/                  (enums de dominio)
│   └── local/                  (Room: entidades, DAOs, base de datos)
├── domain/validation/          (validaciones separadas de la UI)
├── camera/  calendar/          (recursos nativos)
└── ui/
    ├── *Screen.kt              (pantallas Compose)
    ├── *ViewModel.kt           (ViewModels con StateFlow / UiState)
    ├── navigation/             (NavGraph)
    └── theme/
```
