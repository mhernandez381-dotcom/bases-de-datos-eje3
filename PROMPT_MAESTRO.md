# Encargo para el agente implementador

Eres Claude Code, agente principal de implementación. Actúa como arquitecto y desarrollador Java sénior. Implementa un MVP académico pequeño, mantenible y ejecutable: **aplicación de escritorio con login y CRUD de empresas**, perteneciente a un sistema de huella de carbono. No implementes el sistema ambiental completo.

## Contexto obligatorio

Lee primero `CLAUDE.md`, `AGENTS.md`, `PLAN_IMPLEMENTACION.md`, `ESTADO.md` y `referencias/sg_huella_carbono_entrega.sql` desde la raíz de este repositorio. Estos archivos llevan el contexto; no asumas que conoces conversaciones anteriores. Si falta alguno, identifica el faltante antes de inventar un esquema o decisiones.

El usuario conoce Spring Boot, no quiere frontend web separado ni APIs, y quiere evitar instalar Java/MariaDB/Docker en su equipo. El entorno objetivo es GitHub Codespaces accesible desde VS Code local. La interfaz Swing se ve por escritorio remoto noVNC. El agente debe editar y probar en el mismo entorno remoto, cuando esté disponible.

## Alcance cerrado

1. Un usuario demo semilla con contraseña hash, sin registro, CRUD de usuarios, roles ni recuperación de contraseña.
2. Login, sesión en memoria y logout.
3. CRUD Empresa: listar, crear, editar y eliminar lógicamente. Formulario solo NIT y nombre; propietario obtenido de sesión, IDs/fechas automáticos.
4. Conservar relación empresa.id_usuario → usuario.id y restricciones del SQL.
5. No construir consumos, sedes, factores, compensaciones, cálculo CO2, dashboards, mapas, REST o frontend web.

## Stack y arquitectura

- Java 21 objetivo; selecciona y fija una versión estable mantenida de Spring Boot compatible, verificando documentación oficial. Registra versiones exactas. No uses latest o snapshots.
- Swing escrito en Java, layouts estándar, sin instalar Eclipse/WindowBuilder en el remoto.
- Spring Boot no web, `WebApplicationType.NONE`, GUI habilitada con `headless(false)` según API verificada.
- DAO explícitos con Spring JDBC/JdbcTemplate y driver MariaDB. Sin JPA/Hibernate.
- Un proyecto Maven con Wrapper, JUnit y Mockito compatibles.
- Vista → controlador → servicio → DAO → MariaDB. DTO transporta datos; modelo no depende de Swing. Inyección por constructor; no crear abstracciones genéricas innecesarias.
- UI en EDT, consultas/hash fuera del EDT, errores uniformes y cierre limpio de recursos.

## Base de datos y seguridad

Preserva la referencia SQL exactamente. Tiene ocho tablas y un bloque de respaldo; **no ejecutes el archivo completo en cada arranque**. Deriva una inicialización separada para la base operativa, únicamente en una base vacía autorizada. No borres bases existentes ni ocultes un esquema parcial.

Lee los tamaños/tipos/restricciones reales: empresa.nit VARCHAR(20) UNIQUE, nombre VARCHAR(150), id_usuario FK; usuario.correo UNIQUE, password_hash VARCHAR(255); timestamps y eliminado_en. Conserva las otras tablas sin generarles CRUD.

Semilla idempotente en perfil demo: secreto externo, BCrypt, no duplicar cuenta, resetear contraseña o reactivar eliminados automáticamente. Login solo usuario activo. Nunca exponer hash en DTO de sesión ni registrar contraseñas.

Consultas parametrizadas y filtros de propietario/activos. NIT de una empresa eliminada sigue reservado por UNIQUE. Maneja duplicados y filas afectadas; no simules éxito. Borrado lógico únicamente, sin cascadas ni cambios en entidades relacionadas.

Usa credenciales externas, DB user sin root, zona horaria de conexiones explícita y almacenamiento persistente. No publiques MariaDB. Escritorio privado con contraseña no predeterminada. No comitees credenciales, archivos auth de agentes, logs sensibles o volúmenes de datos.

## Procedimiento de trabajo

1. Inspecciona raíz, git status, archivos existentes y si estás en local o Codespaces. No sobrescribas trabajo del usuario. Resume enfoque/impacto/trade-offs brevemente y crea tu lista de tareas.
2. Sigue E0–E5 de `PLAN_IMPLEMENTACION.md` en orden. No generes toda la aplicación sin validar primero el entorno.
3. Si estás local sin stack, limita la primera etapa a generar configuración y estructura; no instales Java/BD/Docker localmente. Registra lo no ejecutado y explica el paso remoto. No finjas que el Codespace está conectado.
4. Si estás remoto, prepara Dev Container, MariaDB y escritorio. Puedes preparar archivos y usar las herramientas permitidas; solicita autorización para instalaciones/operaciones que excedan permisos, recursos, costes o acciones destructivas. Nunca eludas aprobaciones.
5. Tras configurar, valida compilación mínima, conexión MariaDB y ventana Swing visible. Si falta reconstruir el contenedor, guarda el estado antes de pedir la acción. Si no puedes inspeccionar la UI, solicita comprobación concreta al usuario; no inventes la evidencia.
6. Con esa puerta validada, implementa semilla/login y después CRUD, con pruebas por cada comportamiento. Continúa las tareas no bloqueadas sin pedir confirmación por cada clase.
7. Al terminar cada etapa registra cambios, comandos, resultados, errores y pendientes en `ESTADO.md`. No confundas código escrito con código probado.
8. Al cerrar, ejecuta unitarias, integración en MariaDB real y flujo visual. Revisa el diff y secretos. Entrega documentación reproducible y matriz de aceptación con evidencia real.

No crees recursos GitHub, hagas push/merge, modifiques permisos públicos ni elimines datos sin aprobación específica. No lances otras CLI/agentes automáticamente: eres el único implementador. Puedes recomendar una revisión independiente al terminar un hito.

## Criterio de finalización

Desde un Codespace configurado, el usuario debe poder arrancar el programa con el comando documentado, ver Swing, iniciar sesión, crear/listar/editar/eliminar una empresa y confirmar que los datos persisten tras reiniciar. Las restricciones deben funcionar en MariaDB real. El README debe separar instalación inicial, arranque normal, pruebas y respaldo.

No presentes un mock, archivos compilables no compilados, tests omitidos o una captura del editor como aplicación validada. Si algo queda bloqueado, conserva avances e indica exactamente la acción externa requerida.

Empieza ahora por E0 y la primera etapa ejecutable. Comunica en español. Al responder incluye: resumen ejecutivo, cambios, pruebas realmente ejecutadas, riesgos/pendientes y próximo paso.
