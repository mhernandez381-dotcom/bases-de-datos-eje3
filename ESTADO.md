# Estado de implementación

Estados usados: **preparado** (archivo escrito, no ejecutado) · **implementado** (código escrito y compilado) · **probado** (ejecutado con evidencia) · **fallido** · **pendiente**.

## Resumen de etapas

| Etapa | Estado | Evidencia |
|---|---|---|
| E0 — Inspección del entorno | **Probado** | Ver registro 2026-11-08 #1 |
| E1 — Configuración reproducible | **Probado** (réplica con `@devcontainers/cli` dentro del Codespace) | Ver registros #1 y #2 |
| E2 — Ventana + conexión MariaDB | **Probado y cerrado** en el Codespace real (registro #3): BD, VNC con contraseña, ventana vista por el usuario vía noVNC, puerto 6080 *Private* (confirmado por el usuario), cierre con la X → JVM terminada | `docs/evidencias/E2-ventana-diagnostico-codespace-real.png` (+ capturas de réplica) |
| E3 — Esquema, semilla y login | Esquema derivado **probado** en MariaDB 11.8.8; semilla/login **pendientes** | — |
| E4 — CRUD Empresa | Pendiente | — |
| E5 — Pruebas y entrega | Pendiente | — |

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

## Próximo paso

1. ✅ Checkpoint `7c9eecb` (local, **sin push**; un push requiere autorización aparte).
2. ✅ Rebuild real hecho y comprobaciones del agente en el Codespace real (registro #3).
3. ✅ Puerta E2 superada (confirmación visual del usuario + cierre limpio verificado).
4. **Usuario (opcional):** autorizar commit de checkpoint E2 (`ESTADO.md`, `scripts/ejecutar.sh`, evidencia, lock).
5. **Agente:** E3 — semilla demo (perfil `demo`, BCrypt, idempotente), `UsuarioDAO`, autenticación, sesión y pantalla de login, con pruebas unitarias e integración en `sg_huella_carbono_test`.

## Decisiones pendientes de verificación (del kit)

- Rúbrica: suficiencia de un CRUD y aceptación de borrado lógico (a confirmar por el usuario con el docente).
- Push: no autorizado (solo existe el commit local `7c9eecb`).

No guardar secretos ni tokens en este archivo.
