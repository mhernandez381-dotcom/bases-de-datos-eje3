# Plan de implementación — MVP Empresas

## 1. Objetivo y límites

Construir una aplicación de escritorio explicable para la universidad, usando la base del sistema de huella de carbono. La entrega funcional es **login + gestión CRUD de empresas**, no el sistema completo de medición ambiental.

Confirmado por el usuario: Java/Spring Boot, escritorio sin frontend separado, DTO/DAO, una cuenta inicial, sin roles y mínima instalación local. Se propone Codespaces con interfaz Swing escrita en Java. La rúbrica completa de esta fase no se ha verificado: confirmar que acepta un CRUD y borrado lógico antes de la entrega final.

### Incluido

- Cuenta de demostración precargada de forma segura e idempotente.
- Login, sesión en memoria y logout.
- Listar, crear, editar y eliminar lógicamente empresas.
- Validación de campos, mensajes comprensibles y manejo de errores de BD.
- Pruebas unitarias, integración real MariaDB y comprobación visual del flujo.
- Entorno reproducible, instrucciones de ejecución y evidencia honesta.

### Excluido

- Registro/CRUD de usuarios, roles, recuperación de contraseña y autenticación por JWT.
- Pantallas de sedes, consumos, fuentes, factores, proyectos o compensaciones.
- Cálculo CO2, dashboard ambiental, mapas y reportes avanzados.
- API REST, servidor web de la aplicación, Angular/React, JPA/Hibernate.
- Microservicios, infraestructura distribuida, múltiples módulos Maven e instaladores multiplataforma.
- Integrar una API de IA dentro de la aplicación: las IA son herramientas de desarrollo, no una función del MVP.

## 2. Metodología y entorno

**Recomendación:** VS Code local conectado a Codespaces; agente CLI, JDK, compilación, pruebas y MariaDB ejecutados en remoto. El navegador muestra Swing mediante noVNC. No usar la CLI local sobre una copia distinta mientras un agente remoto edita.

Agente principal elegido: Claude Code. Codex o Copilot pueden revisar secuencialmente.

Roles: usuario decide alcance y verifica demo; un agente implementa; un segundo agente/herramienta puede revisar secuencialmente el diff y las evidencias. No se necesita un orquestador multiagente.

Una rama propuesta `feat/mvp-empresas`, con cambios pequeños por etapa. Solo crearla tras comprobar el repositorio y que no hay trabajo ajeno que sobrescribir. Ningún push o publicación sin aprobación. Mantener el estado y comandos en el repositorio para que otro agente continúe.

### Stack objetivo

| Elemento | Decisión / verificación |
|---|---|
| Java | JDK 21 LTS como objetivo; verificar compatibilidad con las versiones elegidas |
| Spring Boot | Versión estable mantenida compatible; fijar versión exacta en POM y registrarla |
| Interfaz | Swing del JDK, layouts estándar; no diseñador Eclipse dentro del remoto |
| Persistencia | Spring JDBC con `JdbcTemplate`, DAO explícitos y driver MariaDB |
| Build | Un proyecto Maven con Wrapper versionado, sin Maven global obligatorio |
| Contraseñas | `PasswordEncoder` con BCrypt del módulo crypto; sin stack de seguridad web |
| BD | MariaDB estable mantenida; fijar imagen/versión y probar el SQL original |
| Pruebas | JUnit y Mockito compatibles con el BOM de Spring Boot; integración MariaDB |
| Desarrollo | Dev Container Linux compatible con desktop-lite; servicio MariaDB separado |

La referencia SQL declara MariaDB 10.4.32; **no obliga a desplegar hoy esa versión antigua**. No modificar el esquema para ocultar incompatibilidades: documentarlas y proponer el ajuste mínimo cuando se detecten. Registrar todas las versiones reales, sin inventarlas en este plan.

### Requisitos operativos

- Configuración de devcontainer y servicios incluida en Git. Verificar que Codespaces construye la imagen y arranca los servicios; no basta JSON válido.
- noVNC por puerto 6080 privado, contraseña distinta de la predeterminada. No publicar 3306 ni VNC sin protección. Verificar visibilidad efectiva en Codespaces.
- Credenciales externas mediante secretos o archivos locales ignorados. No incrustarlas en imágenes, Dockerfiles, scripts ni logs.
- Servicio BD accesible por nombre dentro de la red de contenedores, con healthcheck; no asumir `localhost` entre servicios distintos.
- Usuario de aplicación con privilegios mínimos sobre la base operativa; no usar root en la aplicación. Credenciales de inicialización separadas.
- Almacenamiento persistente de MariaDB y exportación fuera del Codespace. Probar parada/reinicio; una reconstrucción y una eliminación no son equivalentes. No ejecutar `down -v` ni borrar volúmenes para «arreglar» fallos.
- Las dependencias de la CLI del agente y su autenticación se preparan aparte del stack Java. No copiar credenciales dentro de la imagen. Documentar reanudación tras rebuild.

## 3. Fuente SQL y contrato de datos

Referencia incluida: `referencias/sg_huella_carbono_entrega.sql`.
SHA-256: `cd35e68b7139d8c8bf1145807cbbe0c32138f19a41b0425d8ef17c9a82c777a3`.

El archivo original contiene ocho tablas, instalación de `sg_huella_carbono_revision`, creación de una base de respaldo y verificaciones. No es idempotente y no contiene usuarios iniciales. Se ha leído, **no ejecutado** en esta fase.

Para la aplicación:
1. Conservar íntegra la referencia, incluidos nombres y restricciones.
2. Derivar una inicialización del esquema operativo en un archivo separado, sin ejecutar el bloque de respaldo como parte del arranque.
3. Crear esquema solo en una base vacía autorizada. Si ya existe, validar y detenerse ante discrepancias; no hacer DROP ni ocultar un esquema parcial con IF NOT EXISTS indiscriminado.
4. La aplicación normal no crea bases/tablas ni vuelve a cargar el SQL en cada login/inicio.
5. Conservar las ocho tablas; solo Usuario y Empresa necesitan clases de persistencia/pantallas para este alcance. No generar seis CRUD adicionales.

### Usuario

| Columna | SQL | Java / regla |
|---|---|---|
| id | INT, PK, AUTO_INCREMENT | Integer; nulo solo antes de persistir |
| nombre | VARCHAR(100), NOT NULL | String no vacía |
| correo | VARCHAR(150), UNIQUE, NOT NULL | String; aplicar política de normalización consistente |
| password_hash | VARCHAR(255), NOT NULL | Hash BCrypt; nunca exponer a vista ni DTO de sesión |
| creado_en | TIMESTAMP, DEFAULT CURRENT_TIMESTAMP | Fecha de BD |
| actualizado_en | TIMESTAMP, DEFAULT y ON UPDATE CURRENT_TIMESTAMP | Fecha de BD |
| eliminado_en | TIMESTAMP, NULL | NULL = activo |

### Empresa

| Columna | SQL | Regla |
|---|---|---|
| id | INT, PK, AUTO_INCREMENT | No editable |
| id_usuario | INT, NOT NULL, FK usuario.id | Asignado desde sesión; no seleccionar propietario |
| nit | VARCHAR(20), UNIQUE, NOT NULL | Obligatorio, trim, longitud máxima 20; no inventar validación fiscal oficial |
| nombre | VARCHAR(150), NOT NULL | Obligatorio, trim, longitud máxima 150 |
| creado_en | TIMESTAMP, DEFAULT CURRENT_TIMESTAMP | No editable |
| actualizado_en | TIMESTAMP, DEFAULT y ON UPDATE CURRENT_TIMESTAMP | No editable |
| eliminado_en | TIMESTAMP, NULL | Borrado lógico, no DELETE |

Los INT(11) del dump no significan once dígitos de capacidad. Mapear IDs con tipos consistentes. Para timestamps elegir y documentar el mapeo JDBC: por ejemplo LocalDateTime con sesiones de BD en UTC. Verificar la zona de cada conexión, no asumir que el SET SESSION del script se hereda a la aplicación. No guardar fechas como VARCHAR. Mostrar fecha/hora con formato documentado `yyyy-MM-dd HH:mm:ss` y zona explícita.

### Reglas de negocio

- Semilla solo en perfil demo, activación explícita. Correo/nombre configurables, contraseña mediante secreto/entrada protegida; sin contraseña fija en código.
- Insertar la cuenta únicamente si no existe. Un segundo inicio no duplica usuario ni cambia su hash. Si ya existe eliminada, no reactivarla automáticamente. No imprimir contraseñas.
- Login solo con usuario activo. Mensaje genérico ante credenciales inválidas. Sesión en memoria con ID, nombre y correo, nunca contraseña/hash. Logout limpia sesión y pantalla.
- Crear empresa con ID del usuario autenticado. Listar/editar/borrar usando sesión y registros activos, sin ampliar a roles ni administración multiusuario.
- NIT único también frente a empresas eliminadas: no reutilizar ni restaurar automáticamente. Explicar claramente la colisión. No basar la integridad solo en un SELECT previo; manejar la restricción UNIQUE.
- Edición conserva ID, propietario y fecha de creación. Confirmar eliminación y verificar filas afectadas; si no existe o ya está eliminada, informar sin falso éxito.
- Eliminar actualiza `eliminado_en`; conserva fila y referencias. No cascadas de eliminación ni cambios en sedes/compensaciones. No hay restauración en este MVP.
- Si la BD falla: mensaje seguro, log técnico sin secretos, posibilidad de corregir configuración/reintentar. Nunca mostrar stacktrace o SQL crudo al usuario.

## 4. Arquitectura mínima

Un proyecto, un proceso Java, sin HTTP entre capas:

`Vista Swing → Controlador → Servicio → DAO JDBC → MariaDB`

DTO transporta datos; modelo representa Usuario/Empresa. No crear interfaces para cada clase por rutina. Las interfaces DAO sí hacen explícito el contrato y facilitan pruebas.

Estructura orientativa bajo un paquete como `edu.huella`:

| Paquete | Responsabilidad |
|---|---|
| config | Configuración de BD, hash y perfil demo |
| model | Usuario y Empresa, sin dependencias Swing |
| dto | Datos de alta/edición, resumen de empresa y usuario de sesión |
| dao | Contratos UsuarioDAO/EmpresaDAO e implementaciones JDBC |
| service | Autenticación, validación y operaciones del CRUD |
| controller | Conectar eventos de las vistas con servicios |
| view | Login y gestión Empresas |
| session | Estado mínimo de usuario autenticado |
| exception | Errores de aplicación comprensibles, sin jerarquía innecesaria |

Evitar DAO genérico, clases base CRUD, Lombok, MapStruct, eventos distribuidos o mapeadores complejos para dos modelos. Mapeo explícito y breve. Usar inyección por constructor.

Contratos esperados: UsuarioDAO busca por correo y crea semilla; EmpresaDAO lista por propietario, inserta, actualiza y marca eliminación comprobando filas. No devolver mapas sin tipar ni utilizar concatenación de entradas en SQL.

### Swing y arranque

- Configurar Boot como `WebApplicationType.NONE` y `headless(false)` según API de la versión fijada.
- Crear/actualizar componentes en Event Dispatch Thread. BD y BCrypt fuera del EDT, por ejemplo con SwingWorker. Recuperar errores y volver a habilitar botones al terminar.
- No instanciar ventanas al arrancar pruebas unitarias/headless. Separar creación de UI del contexto de servicios.
- Login: campos etiquetados, JPasswordField, botón entrar, Enter, indicador de proceso y errores.
- Empresas: tabla con ID/NIT/nombre, acciones nuevo/editar/eliminar/actualizar/cerrar sesión. Un formulario reutilizado para alta/edición.
- Layouts redimensionables, navegación por teclado, foco y contraste legibles. No posicionamiento absoluto.
- Al cerrar la ventana, cerrar contexto Spring/pool de conexiones y finalizar limpiamente.

## 5. Ejecución por etapas

### E0 — Inspección y preparación
Leer documentos, git status, raíz del repositorio, entorno local/remoto y SQL. Registrar versiones disponibles sin instalar localmente. Revisar documentación oficial para fijar versiones compatibles. Proponer estructura sin borrar trabajo existente.
**Salida:** alcance confirmado en código/documentación, estado inicial y plan de archivos.

### E1 — Configuración reproducible
Crear Dev Container, servicio MariaDB, configuración de escritorio, persistencia y secretos de ejemplo sin valores reales. Preparar proyecto Maven/Wrapper y arranque mínimo Boot + Swing. Crear `.gitignore` antes de manejar credenciales.
**Salida:** archivos construibles, instrucciones exactas de secretos y rebuild. Si se trabaja localmente sin stack, registrar «preparado, no probado» y trasladar validación al Codespace.

### E2 — Puerta de validación remota
Construir el entorno. Verificar Java y Wrapper, MariaDB saludable y conexión real, puerto privado, DISPLAY/escritorio y una ventana Swing visible. Una captura del navegador con noVNC solo acredita aquello que efectivamente se observa.
**Salida:** ventana visible y consulta exitosa; comandos/versiones registrados. No seguir con la UI completa si esta puerta falla. Si el agente carece de herramienta visual, pedir comprobación al usuario y mantener pendiente esa prueba hasta recibirla.

### E3 — Esquema y login
Derivar y ejecutar inicialización solo sobre base vacía autorizada. Implementar semilla, DAO Usuario, autenticación, sesión y pantalla login.
**Salida:** usuario único al reiniciar, hash almacenado, login válido/inválido comprobado y logout posible.

### E4 — CRUD Empresa
Implementar DTO/modelo/DAO/servicio/controlador/vista. Completar validaciones, errores de unicidad, propietario desde sesión, edición y borrado lógico.
**Salida:** recorrido completo visible y persistente, ninguna acción falsa ante fallos.

### E5 — Pruebas, revisión y entrega
Ejecutar matriz de aceptación, resolver hallazgos, revisar diff y secretos. Probar arranque y persistencia tras reinicio. Preparar JAR, guía y evidencia. Revisión independiente opcional, secuencial.
**Salida:** build y pruebas documentados, demostración CRUD, riesgos restantes declarados. Un JAR no equivale a instalador nativo con Java incluido.

En cada etapa actualizar `ESTADO.md`: qué cambió, comando ejecutado, resultado, qué falta y siguiente acción. Preparar cambios pequeños para commit; no publicar sin permiso.

## 6. Matriz mínima de aceptación

| ID | Prueba | Evidencia esperada |
|---|---|---|
| A01 | Construcción Maven | Comando real, código de salida y versión |
| A02 | Semilla ejecutada dos veces | Una cuenta y hash previo conservado |
| A03 | Login correcto/incorrecto/usuario eliminado | Sesión solo en caso válido |
| A04 | Crear empresa | Fila con propietario de sesión y timestamps |
| A05 | NIT/nombre vacíos y excesivos | Rechazo antes de persistir |
| A06 | NIT duplicado | Mensaje comprensible; BD mantiene unicidad |
| A07 | Listar y editar | Datos nuevos; ID/propietario/creación conservados |
| A08 | Borrar lógicamente | Fila conservada, eliminado_en no nulo, ausente de lista |
| A09 | Volver a usar NIT eliminado | Rechazo explícito, sin restauración tácita |
| A10 | ID inexistente/eliminado | Sin mensaje falso de actualización exitosa |
| A11 | Logout | No acceso al CRUD con sesión cerrada |
| A12 | Reinicio aplicación/BD | Datos conservados y semilla no destructiva |
| A13 | Caída de BD | Error seguro y UI sin bloqueo indefinido |
| A14 | Teclado, redimensionado y doble clic | Interfaz usable, sin operaciones duplicadas |
| A15 | Filtros de propietario | No acepta id_usuario proporcionado por formulario |
| A16 | Entorno limpio | Instrucciones permiten reproducir el arranque |

Unitarias: validación, decisiones del servicio, login y resultados DAO simulados. Integración: esquema, UNIQUE/FK, SQL y timestamps en **MariaDB real**, con base de pruebas separada. No usar datos de la demo para pruebas destructivas. Si se usan fixtures de dos propietarios para una prueba, no añadir gestión multiusuario al producto.

Separar tests unitarios sin BD de integración con perfil explícito; documentar comandos reales al implementarlos. No marcar integración aprobada si los tests se omitieron o se usó H2. Para GUI, validar flujo real Swing; no presentar un build o test unitario como QA visual.

## 7. Entregables finales y finalización

- Código fuente Maven, Wrapper, configuración Codespaces y SQL de inicialización separado.
- SQL de referencia sin cambios; bloque de respaldo académico preservado, no ejecutado como arranque.
- README con requisitos, secretos, pasos desde repositorio limpio, ejecución, pruebas, solución de errores y apagado.
- Registro de versiones y pruebas, demo de login/CRUD, JAR y limitaciones de empaquetado.
- Sin secretos ni artefactos de BD en Git; no pedir al profesor credenciales personales de GitHub/IA para ejecutar el programa.

No declarar finalizado si falta compilación, integración MariaDB o prueba del flujo gráfico. Se puede entregar un avance indicando exactamente qué no se validó. El siguiente agente debe poder continuar usando el estado, sin depender del chat original.
