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
MVVM: UI (Compose) → ViewModel (StateFlow) → Repository → Room (SQLite).
Modelo de datos en 3FN con claves primarias, foráneas e índices.

## Cuentas de prueba
| Rol | Correo | Clave |
|---|---|---|
| Supervisor | supervisor@ficticio.cl | duoc123 |
| Ejecutivo | ejecutivo1@ficticio.cl | duoc123 |
| Ejecutivo | ejecutivo2@ficticio.cl | duoc123 |
| Ejecutivo | ejecutivo3@ficticio.cl | duoc123 |

## Instrucciones de ejecución
1. Abrir la carpeta en Android Studio **Koala (2024.1.1) o superior** con JDK 17 (incluido en Android Studio).
2. Esperar la sincronización de Gradle (descarga Gradle 8.7 y genera `local.properties`; requiere internet la primera vez).
3. Ejecutar en emulador o dispositivo con API 26+ (recomendado API 34).
4. La base de datos se precarga sola con datos ficticios en el primer arranque.

## Estructura
```
app/src/main/java/cl/duoc/oportunidades/
├── MainActivity.kt
├── data/            (modelos, Room, repositorio, sesión)
├── domain/          (validaciones separadas de la UI)
├── camera/          (captura y conservación de fotos)
├── calendar/        (integración con calendario)
└── ui/              (theme, viewmodels, navigation, screens)
```
