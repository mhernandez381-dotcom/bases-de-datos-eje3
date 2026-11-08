# Estado de implementación

Estados usados: **preparado** (archivo escrito, no ejecutado) · **implementado** (código escrito y compilado) · **probado** (ejecutado con evidencia) · **fallido** · **pendiente**.

## Resumen de etapas

| Etapa | Estado | Evidencia |
|---|---|---|
| E0 — Inspección del entorno | **Probado** | Ver registro 2026-11-08 #1 |
| E1 — Configuración reproducible | **Probado** (réplica con `@devcontainers/cli` dentro del Codespace) | Ver registros #1 y #2 |
| E2 — Ventana + conexión MariaDB | **Probado y cerrado** en el Codespace real (registro #3): BD, VNC con contraseña, ventana vista por el usuario vía noVNC, puerto 6080 *Private* (confirmado por el usuario), cierre con la X → JVM terminada | `docs/evidencias/E2-ventana-diagnostico-codespace-real.png` (+ capturas de réplica) |
| E3 — Esquema, semilla y login | **Probado** (registro #4): 12 unitarias + 10 integración MariaDB real OK; semilla idempotente en la base operativa; login inválido/válido, logout, BD caída y cierre comprobados en el display `:1` con capturas. **Pendiente**: que el usuario lo repita en noVNC (opcional, recomendado) | `docs/evidencias/E3-*.png` |
| E4 — CRUD Empresa | **Probado** (registro #5): 20 unitarias + 23 integración MariaDB real OK; recorrido visual completo en `:1` sobre la base de pruebas. **Pendiente**: repetición del usuario en noVNC (recomendada) | `docs/evidencias/E4-*.png` |
| E5 — Pruebas y entrega | **Probado** (registro #6): A12/A13 con reinicio real de MariaDB, respaldo/restauración, clon limpio, revisión de secretos y de código, JAR. **Pendiente del usuario**: Codespace nuevo desde cero (opcional), revisión independiente (opcional), decisión sobre hooks LFS y push | `docs/evidencias/E5-*.png` |

## Versiones fijadas (verificadas en fuentes oficiales el 2026-11-08)

| Elemento | Versión | Fuente / motivo |
|---|---|---|
| JDK | 21 (Microsoft Build of OpenJDK 21.0.13 en el Codespace previo; imagen `mcr.microsoft.com/devcontainers/java:3.1.7-21-bookworm` en el Dev Container) | LTS objetivo del plan |
| Spring Boot | 4.1.2 (GA 2026-10-22; OSS hasta 2027-07-31) | api.spring.io / Maven Central; docs: Java 17–27 |
| Spring Framework / JDBC | 7.0.10 (BOM) | `dependency:list` |
| spring-security-crypto | 7.1.2 (BOM), solo BCrypt | `dependency:list` |
| HikariCP | 7.0.2 (BOM) | `dependency:list` |
| MariaDB Connector/J | 3.5.10 (BOM) | `dependency:list`; opciones `connectionTimeZone`/`forceConnectionTimeZoneToSession` verificadas con `javap` |
| JUnit Jupiter / Mockito / AssertJ | 6.0.3 / 5.23.0 / 3.27.7 (BOM) | `dependency:list` |
| Maven (Wrapper) | 3.9.16, wrapper 3.3.4 `only-script`, SHA-256 fijado | SHA-512 oficial comprobado |
| MariaDB servidor | 11.8.8 (LTS) | Docker Hub; 11.8.9 descartada por tener < 2 semanas |
| desktop-lite / node / claude-code (features) | 1.2.10 / 2.1.1 / 1.0.5 | Etiquetas comprobadas en ghcr.io |

## Decisiones tomadas

- **Kit movido a la raíz** (estaba en `kit_implementacion_mvp 3/`): `.devcontainer/` debe estar en la raíz y `CLAUDE.md` se carga desde ahí. El `README.md` del kit se conservó como `docs/README_KIT.md` porque ya existía un `README.md` raíz. SHA-256 del SQL sin cambios.
- **Rama local** `feat/mvp-empresas` (sin commits ni push todavía).
- **Base operativa** `sg_huella_carbono_revision` (la que define la sección 1 del SQL); **base de pruebas** separada `sg_huella_carbono_test`.
- **Esquema**: `db/esquema/tablas.sql` generado por `db/derivar-esquema.sh` (líneas 35-39 y 42-335 de la referencia; sin CREATE DATABASE/USE, sin datos, sin respaldo). `--verificar` detecta divergencias.
- **Inicialización**: `db/init/01-inicializar.sh`, ejecutado por el entrypoint oficial de MariaDB **solo con volumen vacío**. Crea las dos bases, aplica el esquema y crea `huella_app` (SELECT, INSERT, UPDATE; sin DELETE ni DDL) y `huella_test` (ALL solo sobre la base de pruebas).
- **Secretos**: `.devcontainer/.env` (ignorado por Git, 600) generado por `init-secrets.sh` como `initializeCommand` solo si no existe. `env.example` sin valores.
- **MariaDB no publicada** (sin `ports:`), accesible como `db` en la red de compose. Volumen nombrado `huella-mariadb-data`.
- **noVNC 6080** único puerto reenviado; la contraseña de build de desktop-lite es provisional y `post-start.sh` la sustituye por `HUELLA_VNC_PASSWORD` del `.env` en cada arranque.
- **Zona horaria**: servidor `--default-time-zone=+00:00`; cada conexión fuerza UTC (driver + `connection-init-sql`). Verificado: `@@session.time_zone = +00:00` desde la app.
- **Arranque tolerante**: `initialization-fail-timeout=-1` para que la UI muestre error y permita reintentar si la BD no responde (A13).

## Registro

### 2026-11-08 #1 — Codespace `ubiquitous-rotary-phone-...` (imagen universal por defecto), rama `feat/mvp-empresas`, sin commits

**Etapa:** E0 + E1 + E2 parcial.

**Entorno encontrado:** Codespace real (`CODESPACES=true`), Ubuntu 24.04, 2 CPU / 7.8 GiB, JDK 21.0.13 y 25 (SDKMAN), Maven 3.10.0 global (no usado), Docker 29.9.2 (docker-in-docker), sin MariaDB ni escritorio, Claude Code 2.1.319.

**Archivos creados:** `.gitignore`, `.devcontainer/{devcontainer.json,docker-compose.yml,Dockerfile,init-secrets.sh,post-start.sh,env.example}`, `db/derivar-esquema.sh`, `db/esquema/tablas.sql`, `db/init/01-inicializar.sh`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `src/main/java/edu/huella/{HuellaApplication.java,config/DiagnosticoConexion.java}`, `src/main/resources/application.properties`, `docs/evidencias/E2-ventana-diagnostico-xvfb.png`.

**Comandos y resultados (ejecutados de verdad):**
- `sha256sum referencias/sg_huella_carbono_entrega.sql` → `cd35e68b…a3` (coincide).
- `db/derivar-esquema.sh && db/derivar-esquema.sh --verificar` → OK, 8 `CREATE TABLE`.
- `docker compose --env-file <temporal> -p huella-prevalidacion up -d db` (secretos temporales en scratchpad, fuera del repo) → contenedor `healthy`; log: `huella-init: completado`. Sin errores del esquema en 11.8.8 (solo avisos de io_uring/cgroup del kernel del Codespace).
- Como `huella_app`: `VERSION()=11.8.8-MariaDB-ubu2404`, `@@global/@@session.time_zone=+00:00`, 8 tablas, `SHOW GRANTS` = SELECT, INSERT, UPDATE. `SHOW CREATE TABLE empresa` conserva `UNIQUE KEY nit`, `empresa_ibfk_1`. `docker port` vacío (no publicado).
- `./mvnw -B -q package -DskipTests` (JDK 21) → JAR `target/huella-empresas-0.1.0.jar`.
- `java -jar target/huella-empresas-0.1.0.jar` con `DISPLAY=:99` (Xvfb instalado temporalmente con apt en el contenedor previo) → log `Diagnóstico BD: version=11.8.8…, zona=+00:00, tablas=[8]`; `xwininfo` muestra la ventana; captura revisada visualmente.

**No ejecutado / pendiente:**
- Construcción del Dev Container en Codespaces (en curso: prueba local con `@devcontainers/cli@0.89.0 up`).
- noVNC por el puerto 6080, privacidad del puerto y contraseña VNC (requiere rebuild).
- Cierre limpio de la ventana (no se probó el cierre; el proceso se detuvo con kill).
- Pruebas unitarias e integración: aún no existen.

### 2026-11-08 #2 — Réplica del Dev Container dentro del mismo Codespace (docker-in-docker)

**Etapa:** E1 (validación de construcción) + E2.

**Comandos y resultados:**
- `npx -y @devcontainers/cli@0.89.0 up --workspace-folder .` → `outcome: success`; `init-secrets.sh` creó `.devcontainer/.env`; `post-start.sh` ejecutado; `db` healthy. Generó `.devcontainer/devcontainer-lock.json` (digests de features; se conserva).
- **Defecto 1 (corregido):** `.env` quedó `rw-rw-rw-` pese a `umask 077` (ACL por defecto de `/workspaces`). `init-secrets.sh` ahora fuerza `chmod 600` y `setfacl -b`. Verificado: `-rw-------`.
- **Defecto 2 (seguridad, corregido):** desktop-lite 1.2.10 arrancaba Xtigervnc con `-SecurityTypes None` (sin contraseña) porque su script de arranque comprueba `VNC_PASSWORD` en tiempo de ejecución y la variable solo existe al construir. Corrección: `VNC_PASSWORD` en el entorno del servicio `app` (desde `.env`).
- **Defecto 3 (seguridad, corregido):** tras lo anterior la contraseña efectiva era la provisional de construcción, porque `tigervncserver` copia el archivo de contraseña al iniciar. `post-start.sh` ahora reescribe el archivo y reinicia Xtigervnc. Prueba RFB real (cliente Java mínimo con DES del JDK, en scratchpad, fuera del repo): contraseña del `.env` → **OK**; provisional → FALLO; vacía → FALLO; `vscode` → FALLO.
- Dentro del contenedor `app` (usuario `vscode`): JDK 21.0.13 (Microsoft), `DISPLAY=:1`, `TZ=UTC`, Claude Code 2.1.319, Node 22.23.4. `ss`: noVNC en `0.0.0.0:6080`, VNC solo en `127.0.0.1:5901`. `curl /vnc.html` → HTTP 200.
- `mariadb -h db` como `huella_app` → 11.8.8, `+00:00`, 8 tablas; como `huella_test` → acceso a `sg_huella_carbono_test`; `huella_app` sobre la base de pruebas → `ERROR 1044` (aislamiento correcto).
- `./mvnw -v` descargó Maven 3.9.16 con verificación SHA-256; `./mvnw -B -q package -DskipTests` → exit 0.
- `java -jar target/huella-empresas-0.1.0.jar` → log `Diagnóstico BD: version=11.8.8…, zona=+00:00, tablas=[8]`; `xwininfo` y captura del display `:1` muestran la ventana con decoración de fluxbox.
- Cierre: `xdotool windowclose` (destrucción X, no es un cierre de usuario) dejó la JVM viva — artefacto de la prueba. Con cierre real del gestor (`Alt+F4`) **la JVM terminó en 3 s**.
- Contenedor y volumen desechables `huella-prevalidacion` eliminados (solo datos de prueba propios). La réplica `huella-mvp` sigue en el docker-in-docker del Codespace actual y desaparecerá con el rebuild (el Codespace real creará su propio volumen vacío y lo inicializará con el mismo `.env`).

**No validado todavía (requiere al usuario):**
- Rebuild real en Codespaces: `initializeCommand` en el host del Codespace, compose sobre el Docker del host, reenvío del puerto 6080 y su visibilidad *Private*.
- Ver la ventana en el navegador mediante noVNC introduciendo `HUELLA_VNC_PASSWORD` (yo solo inspeccioné capturas del display `:1`).

**Archivos añadidos en #2:** `.devcontainer/devcontainer-lock.json`, `scripts/ejecutar.sh`, `docs/evidencias/E2-ventana-diagnostico-devcontainer-display1.png`, `README.md` (ampliado, conserva el texto original).

### 2026-11-08 #3 — Codespace real tras *Rebuild Container* (Dev Container del repo)

**Etapa:** puerta de validación E2.

**Repositorio:** raíz `/workspaces/bases-de-datos-eje3`, rama `feat/mvp-empresas`, HEAD `7c9eecb` (verificado, sin reset/checkout). Cambios sin commit encontrados: `ESTADO.md` (nota del checkpoint) y `.devcontainer/devcontainer-lock.json` (solo falta el salto de línea final, regenerado por el rebuild; contenido idéntico). Ambos conservados.

**Comprobado (ejecutado de verdad):**
- Entorno: `CODESPACES=true`, Debian 12 (imagen del Dev Container), usuario `vscode`, `DISPLAY=:1`, `TZ=UTC`, JDK 21.0.13, `db` resuelve en la red de compose.
- `.devcontainer/.env` existe con permisos `-rw-------` (valores no mostrados).
- MariaDB como `huella_app`: `11.8.8-MariaDB-ubu2404`, zona global/sesión `+00:00`, 8 tablas en `sg_huella_carbono_revision`, grants SELECT/INSERT/UPDATE.
- Escritorio: Xtigervnc con `-SecurityTypes VncAuth` solo en `127.0.0.1:5901`; noVNC (websockify) en `0.0.0.0:6080`, `curl /vnc.html` → 200. El archivo de contraseña base y el que usa Xtigervnc coinciden (hash) con `vncpasswd -f` de `HUELLA_VNC_PASSWORD` → `post-start.sh` aplicó la contraseña del `.env`.
- `./mvnw -B -q package -DskipTests` → OK. `java -jar` con `DISPLAY=:1` → log `Diagnóstico BD: version=11.8.8…, zona=+00:00, tablas=[8]`; `xwininfo`: ventana «Huella de carbono — diagnóstico» `IsViewable`; captura del display revisada (MariaDB 11.8.8, zona +00:00, «Tablas: 8/8 OK»).

**Defecto encontrado y corregido:** `scripts/ejecutar.sh --demo` hacía `source` del `.env`, que falla en bash porque `HUELLA_DEMO_NOMBRE` lleva un espacio sin comillas (formato de compose). Ahora se exportan solo las tres variables `HUELLA_DEMO_*` leídas con `grep`. Verificado con `bash -n` y lectura de prueba (sin mostrar valores). El modo `--demo` en sí aún no tiene efecto (semilla = E3).

**Confirmación del usuario (2026-11-08):** puerto 6080 *Private*, conexión noVNC con contraseña y ventana con «Tablas: 8/8 OK» vistas en el navegador. Tras cerrarla, el agente comprobó: proceso 7104 terminado (`kill -0` falla), ninguna JVM de la app viva y ninguna ventana «Huella» en `:1` → **cierre limpio**. Puerta E2 superada.

**No verificable por el agente:** la visibilidad *Private* del puerto 6080 (no hay `gh` en el contenedor; `devcontainer.json` no puede fijar visibilidad, el valor por defecto de Codespaces es privado) y la vista real en el navegador por noVNC. El cierre con la X lo hará el usuario sobre la ventana que quedó abierta.

### 2026-11-08 #4 — E3: semilla, DAO Usuario, autenticación, sesión y login (Codespace real)

**Commit previo autorizado:** `19bdd62` (checkpoint E2, local, sin push).

**Archivos nuevos:** `model/Usuario`, `dto/UsuarioSesion` (id, nombre, correo: sin hash), `dao/UsuarioDAO` + `UsuarioDaoJdbc`, `service/AutenticacionService`, `service/Correos` (normalización: `strip` + minúsculas), `session/SesionUsuario`, `exception/AplicacionException` + `CredencialesInvalidasException`, `config/SeguridadConfig` (BCrypt coste 10), `config/SemillaDemo` (`@Profile("demo")`), `controller/LoginControlador` + `EmpresasControlador` (solo logout en E3), `view/LoginVista`, `view/EmpresasVista` (cabecera de sesión + «pendiente E4»), `view/AplicacionUI` (navegación y cierre), `resources/application-demo.properties`. Pruebas en `src/test/...`: `AutenticacionServiceTest`, `SemillaDemoTest`, `UsuarioDaoJdbcIT`, `SemillaDemoIT`, `AutenticacionServiceIT`, `soporte/BaseDatosPrueba`, `resources/application-integracion.properties`.
**Modificados:** `HuellaApplication` (abre el login), `README.md` (arranque `--demo`, pruebas). **Eliminado:** `config/DiagnosticoConexion` (ventana provisional de E2; queda en el historial de Git).

**Decisiones de diseño:**
- La semilla busca el correo **incluyendo eliminados**: si existe, no inserta, no cambia el hash y no reactiva. Si dos arranques compiten, el `UNIQUE` de la BD gana (`DuplicateKeyException` → «ya existe»). Una configuración demo inválida aborta el arranque (el perfil se activó explícitamente); con la BD caída se registra un aviso y la UI arranca igual.
- Login solo con `eliminado_en IS NULL`. Mismo mensaje para clave incorrecta, correo inexistente o usuario eliminado; con correo inexistente se compara contra un hash ficticio para igualar el tiempo. Se rechazan contraseñas de más de 72 bytes (límite de BCrypt).
- BD y BCrypt en `SwingWorker`; el formulario se bloquea mientras se verifica (evita envíos dobles) y la contraseña se borra del campo y del `char[]`.
- Cerrar cualquier ventana (X / Alt+F4) cierra sesión, libera la ventana y cierra el contexto Spring en otro hilo.
- Las IT solo actúan si `DATABASE() = sg_huella_carbono_test`; aplican el esquema derivado si la base está vacía, vacían con `DELETE` (orden de FK) y fallan ante un esquema parcial (sin DROP).

**Comandos y resultados (ejecutados de verdad):**
- `./mvnw -B test` → 12 pruebas, 0 fallos (`AutenticacionServiceTest` 7, `SemillaDemoTest` 5).
- `./mvnw -B verify -Dintegracion` → unitarias 12 OK + integración 10 OK (`AutenticacionServiceIT` 3, `UsuarioDaoJdbcIT` 5, `SemillaDemoIT` 2), BUILD SUCCESS, exit 0. Repetido tras el último cambio de la vista: mismo resultado. La base `sg_huella_carbono_test` tiene ahora las 8 tablas (aplicadas por la IT), con `PRIMARY KEY` y `UNIQUE KEY correo`. Sesión `+00:00` y `VERSION()` MariaDB comprobadas dentro de la IT.
- **A02 en la base operativa:** `scripts/ejecutar.sh --demo` dos veces. Arranque 1: log «Semilla demo: cuenta creada (id=1)»; fila `Usuario Demo / demo@huella.local / $2a$…`, `eliminado_en` NULL. Arranque 2: «la cuenta ya existe; no se modifica»; `COUNT(*)=1`; SHA-256 del `password_hash` idéntico antes y después. La contraseña demo no aparece en ningún log (grep = 0).
- **A03/A11 visual (display `:1`, xdotool + capturas revisadas por el agente):** clave incorrecta → «Correo o contraseña incorrectos.», campo de contraseña vacío (`E3-login-invalido.png`); clave correcta (tecleada desde el `.env` sin mostrarla) → ventana «Empresas» con «Sesión: Usuario Demo (demo@huella.local)» (`E3-login-ok.png`); Alt+S «Cerrar sesión» → login vacío, log «Sesión cerrada: usuario id=1» (`E3-logout.png`).
- **A13:** app con `HUELLA_DB_HOST=127.0.0.1 HUELLA_DB_PORT=1` → el login aparece; al entrar, ~5 s (timeout de Hikari) y mensaje «No se pudo conectar con la base de datos. Inténtelo de nuevo en unos momentos.»; log `WARN … (CannotGetJdbcConnectionException)` sin SQL, URL ni secretos. **Defecto encontrado y corregido:** el mensaje se cortaba («Inténtelo de n…»); ahora hace salto de línea (`E3-bd-no-disponible.png`).
- **Cierre:** con BD disponible, Alt+F4 → JVM terminada en ~1,5 s; con BD caída, ~5 s. (Un primer «JVM viva» fue un falso positivo: `pgrep -f` coincidía con mi propio comando; verificado con `kill -0 <PID>`.)

**No probado / pendiente:**
- Comprobación visual del usuario en noVNC (las capturas son del display `:1` tomadas por el agente, no del navegador).
- Redimensionado y navegación completa por teclado (A14) solo se probó parcialmente (Tab, Enter, Alt+S, Alt+F4).
- Reinicio de la BD (A12) aún no probado.

**Observación del entorno:** `.git/hooks` contiene hooks de Git LFS (`post-commit`, `pre-push`, …) pero `git-lfs` no está instalado y el repo no tiene `.gitattributes`. El commit funciona con un aviso; **el hook `pre-push` probablemente bloqueará un push** hasta instalar git-lfs o retirar esos hooks (requiere decisión del usuario).

### 2026-11-08 #5 — E4: CRUD Empresa (Codespace real)

**Commit previo autorizado:** `6fbf534` (E3, local, sin push).

**Archivos nuevos:** `model/Empresa`, `dto/EmpresaFormulario` (solo NIT y nombre: no hay campo de propietario), `dto/EmpresaResumen`, `exception/ValidacionException`, `dao/EmpresaDAO` + `EmpresaDaoJdbc`, `service/EmpresaService`, `view/EmpresasTablaModelo`, `view/EmpresaFormularioDialogo` (un formulario modal reutilizado para alta y edición), `view/FocoDialogo`. Pruebas: `EmpresaServiceTest` (8), `EmpresaServiceIT` (10), `EmpresaDaoJdbcIT` (3).
**Modificados:** `view/EmpresasVista` (tabla + acciones), `controller/EmpresasControlador` (CRUD con SwingWorker), `view/AplicacionUI`, `HuellaApplication`, `README.md`.

**Decisiones de diseño:**
- El propietario sale siempre de `SesionUsuario.requerir()`; todas las consultas filtran `id_usuario = ? AND eliminado_en IS NULL`. Sin sesión no se toca la BD.
- Validación antes de la BD: `strip`, obligatorios y longitud en **caracteres** (code points, como VARCHAR): NIT ≤ 20, nombre ≤ 150. No se inventa validación fiscal.
- Unicidad del NIT garantizada por el `UNIQUE` de MariaDB (no por un SELECT previo). Tras `DuplicateKeyException` se consulta el NIT solo para explicar: «Ya existe una empresa registrada con el NIT X» o «pertenece a una empresa eliminada… no se reutilizan… no se restaura».
- `UPDATE` devuelve filas **encontradas** (Connector/J `useAffectedRows=false` por defecto): guardar sin cambios no es un falso error; 0 filas = inexistente/ajena/eliminada → mensaje y recarga, sin falso éxito.
- Borrado lógico con `UPDATE … SET eliminado_en = CURRENT_TIMESTAMP`; sin DELETE (el usuario `huella_app` ni siquiera tiene ese permiso), sin cascadas.
- Una operación de BD a la vez: botones desactivados y acciones ignoradas mientras hay un SwingWorker en curso.
- Fechas mostradas `yyyy-MM-dd HH:mm:ss` con «(UTC)» en la cabecera.

**Comandos y resultados (ejecutados de verdad):**
- `./mvnw -B verify -Dintegracion` → unitarias 20/20, integración 23/23, BUILD SUCCESS, exit 0 (ejecutado tras el último cambio de código). `db/derivar-esquema.sh --verificar` → OK; SHA-256 de la referencia sin cambios.
- QA visual en display `:1` con xdotool y capturas revisadas, apuntando la app a `sg_huella_carbono_test` (la base operativa sigue con 1 usuario y 0 empresas): login; Alt+N + Enter vacío → «El NIT es obligatorio. El nombre es obligatorio.»; alta de dos empresas; NIT duplicado → mensaje; edición (nombre cambiado, `creado_en` igual, `actualizado_en` nuevo); Supr → confirmación (Enter = Cancelar no borra, 3 rondas); confirmar → fila con `eliminado_en`, fuera de la lista; reutilizar NIT eliminado → mensaje explicativo; reinicio de la app → datos conservados; triple Enter en Guardar → **1** fila; doble clic → edición; redimensionado 560×340 y 1300×650; Alt+S → login; Alt+F4 → JVM terminada (~1 s).

**Defectos encontrados en la QA visual y corregidos:**
1. **Foco perdido tras guardar:** al desactivar el botón enfocado durante la recarga, la ventana quedaba sin foco y los atajos (Alt+N…) dejaban de responder; además un texto tecleado acabó en un formulario de edición. Corrección: `EmpresasVista.restaurarFoco()` devuelve el foco a la tabla al terminar cada operación.
2. **Diálogos modales sin foco bajo fluxbox/noVNC:** el formulario y la confirmación aparecían sin foco de teclado (Enter/Escape se perdían). Corrección: `FocoDialogo` pide el foco al mostrarse y reintenta hasta 6 veces (120 ms). Verificado con `xdotool getwindowfocus` en varias rondas.
3. En edición, el NIT queda seleccionado al abrir (escribir reemplaza en vez de añadir). Tras el login, la tabla recibe el foco con la primera fila seleccionada (operable solo con teclado).

**Incidencias de la automatización (no son defectos de la app):** una instancia antigua quedó abierta porque `pgrep -f` devolvió un PID equivocado y recibió algunas teclas; ahora el PID se obtiene con `xdotool getwindowpid`. La base de pruebas tenía la cuenta demo de `SemillaDemoIT` con otra contraseña: la semilla, correctamente, no la modificó (se vació la base de pruebas para la QA).

**Observaciones menores / pendiente:**
- A 560 px de ancho las columnas de fecha se recortan con «…» (se ven completas al ampliar).
- El driver MariaDB registra en WARN el error 1062 (`Duplicate entry 'NIT' for key 'nit'`) cuando hay duplicado: log técnico sin secretos; el usuario solo ve el mensaje traducido.
- Con la BD caída el cierre tarda ~5 s (pool intentando conectar); con BD disponible ~1 s.
- Pendiente E5: A12 (reinicio de **MariaDB**), A16 (entorno limpio), revisión de diff/secretos, JAR y guía final; repetición por el usuario en noVNC.

### Matriz de aceptación (actualizada en E5)

| ID | Estado | Evidencia |
|---|---|---|
| A01 Build | Probado | `./mvnw -B verify -Dintegracion` exit 0 |
| A02 Semilla ×2 | Probado (operativa + IT) | registro #4, `SemillaDemoIT` |
| A03 Login ok/mal/eliminado | Probado (IT + visual) | `AutenticacionServiceIT`, `E3-*.png` |
| A04 Crear | Probado (IT + visual) | `EmpresaServiceIT.a04…`, `E4-03` |
| A05 Vacíos/excesivos | Probado (unit + IT + visual) | `E4-02` |
| A06 NIT duplicado | Probado (IT + visual) | `E4-04` |
| A07 Listar/editar | Probado (IT + visual) | `E4-05` |
| A08 Borrado lógico | Probado (IT + visual) | `E4-14`, `E4-15` |
| A09 NIT eliminado | Probado (IT + visual) | `E4-08` |
| A10 ID inexistente/eliminado | Probado (IT) | `EmpresaServiceIT.a10…` |
| A11 Logout | Probado (IT + visual) | `E3-logout.png` |
| A12 Reinicio app/BD | Probado: app (`E4-10`) y MariaDB real (registro #6) | `E5-recuperada.png` |
| A13 Caída de BD | Probado en login y en el CRUD con MariaDB realmente apagada | `E3-bd-no-disponible.png`, `E5-bd-caida.png` |
| A14 Teclado/redimensionado/doble clic | Probado (visual) | `E4-11`, `E4-12`, `E4-13` |
| A15 Propietario | Probado (IT con dos propietarios) | `EmpresaServiceIT.a15…` |
| A16 Entorno limpio | Probado parcialmente: clon limpio compila y pasa 20+23 pruebas; Codespace nuevo desde cero no probado (requiere al usuario) | registro #6 |

### 2026-11-08 #6 — E5: pruebas finales, respaldo, revisión y entrega (Codespace real)

**Commit previo autorizado:** `9097856` (E4, local, sin push). Reinicio de MariaDB autorizado por el usuario.

**A12/A13 — reinicio real de MariaDB (sin tocar el volumen):** dentro del contenedor `app` no hay CLI de Docker, así que se envió `SHUTDOWN` como root (cuenta del `.env`); la política `restart: unless-stopped` relanzó el contenedor (`Uptime` 3006 s → 6 s). Con la app abierta (base de pruebas) y la BD apagada, F5 → en ~5 s «No se pudo conectar con la base de datos…», ventana usable y lista anterior conservada (`E5-bd-caida.png`). Con la BD de vuelta, F5 → «Lista actualizada.» sin reiniciar la app (`E5-recuperada.png`) y una edición se guardó. Datos conservados en la base de pruebas; en la operativa, 1 usuario con SHA-256 del hash idéntico antes y después.

**Respaldo y restauración (nuevo):** `scripts/respaldar.sh` (cuenta de la app, `--single-transaction`, sin `CREATE DATABASE`/`USE`/`DROP TABLE`, salida `respaldos/*.sql.gz` con permisos 600, ignorada por Git; credenciales en archivo temporal 600, no en la línea de comandos) y `scripts/restaurar.sh` (root; solo en base vacía; `--crear` opcional). Probado: respaldo de la base operativa y de la de pruebas; restaurar sobre `sg_huella_carbono_test` (con tablas) → rechazado, exit 1, sin cambios; restaurar en `sg_huella_carbono_restauracion_prueba` (creada con `--crear`) → 8 tablas, `CHECKSUM TABLE` idéntico en las 8 tablas, `UNIQUE KEY nit` y `empresa_ibfk_1` presentes. **Esa base de prueba se eliminó después con autorización del usuario** (`DROP DATABASE sg_huella_carbono_restauracion_prueba`; solo contenía la copia de datos de prueba). Quedan `sg_huella_carbono_revision` y `sg_huella_carbono_test`.

**A16 — entorno limpio (parcial):** `git clone` del repo a una carpeta vacía del scratchpad → `./mvnw -B verify -Dintegracion` → 20 + 23 OK; `db/derivar-esquema.sh --verificar` OK. Confirma que el repo es autosuficiente (sin `.env` versionado; las IT usan las variables del contenedor). No se creó un Codespace nuevo (crea recursos con coste: acción del usuario).

**Revisión de código y secretos:**
- **Defecto de seguridad encontrado y corregido:** el renderizador por defecto de `JTable` interpreta como HTML un texto que empiece por `<html>`; un nombre de empresa así se renderizaba (y podría cargar imágenes remotas). Ahora las celdas usan un renderizador con `html.disable`. Verificado: `<html><b>NEGRITA</b> <i>cursiva</i>` se ve literal (`E5-html.png`). Los demás textos con datos del usuario no empiezan por `<html>` o ya se escapan (mensajes de login y formulario).
- Revisado: SQL siempre parametrizado (la única concatenación es de nombres de tabla fijos en el soporte de pruebas); sin SQL ni trazas en la UI; logs sin contraseñas, hashes ni URL con credenciales; BD y BCrypt fuera del EDT; propietario solo desde la sesión.
- Ningún valor `*PASSWORD` del `.env` aparece en archivos versionados/nuevos ni en el historial (`git log -p --all`). `target/`, `.env` y `respaldos/` ignorados.

**Build final:** `./mvnw -B clean verify -Dintegracion` → 20/20 unitarias, 23/23 integración, BUILD SUCCESS, exit 0. JAR `target/huella-empresas-0.1.0.jar` (11 313 170 bytes) con JDK 21.0.13 y Maven 3.9.16. No es un instalador (necesita Java 21, `DISPLAY` y las variables `HUELLA_DB_*`).

**README:** estado actualizado; nuevas secciones «Respaldo y restauración» y «Ejecutable (JAR) y entrega»; tabla de problemas ampliada.

**Riesgos y limitaciones conocidas:**
- Hooks de Git LFS: eran los 4 stubs estándar (`post-checkout`, `post-commit`, `post-merge`, `pre-push`; este último salía con código 2 sin `git-lfs` y habría bloqueado el push). El repo no usa LFS (sin `.gitattributes`). **Retirados con autorización del usuario**: movidos a `.git/hooks-lfs-retirados/` (local, no versionado, reversible moviéndolos de vuelta). Si se reconstruye el Codespace o se vuelve a clonar, podrían reaparecer.
- El foco de los diálogos depende del gestor de ventanas del escritorio remoto; mitigado con `FocoDialogo` y verificado en fluxbox, pero un clic en el diálogo siempre funciona.
- Columnas de fecha recortadas en ventanas muy estrechas; WARN del driver en NIT duplicado; cierre de ~5 s con BD caída.
- Rúbrica: aún sin confirmar con el docente si basta un CRUD y si se acepta el borrado lógico.
- Las capturas son del display `:1` tomadas por el agente; el usuario confirmó visualmente E2 en noVNC, pero no ha repetido E3–E5 en el navegador.

## Próximo paso

1. ✅ Checkpoints locales `7c9eecb` (E0–E2), `19bdd62` (puerta E2), `6fbf534` (E3), `9097856` (E4) y el commit de E5. **Sin push.**
2. ✅ Hooks LFS retirados y base de restauración eliminada (autorizado).
3. **Usuario:** decidir el push de `feat/mvp-empresas` y el PR a `main` (no autorizado todavía).
4. **Usuario (recomendado):** repetir el recorrido en noVNC; revisión independiente secuencial con `PROMPTS_REVISION.md`; confirmar la rúbrica con el docente.

## Decisiones pendientes de verificación (del kit)

- Rúbrica: suficiencia de un CRUD y aceptación de borrado lógico (a confirmar por el usuario con el docente).
- Push: no autorizado todavía (todos los commits son locales).

No guardar secretos ni tokens en este archivo.
