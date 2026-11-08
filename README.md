# bases-de-datos-eje3
proyecto y taller del eje 3, en la asignatura bases de datos

## Huella de carbono — MVP Empresas (escritorio)

Aplicación de escritorio **Java 21 + Spring Boot 4.1.2 (no web) + Swing + Spring JDBC + MariaDB 11.8.8**: login con una cuenta demo y CRUD de empresas con borrado lógico. No es el sistema ambiental completo (sin consumos, sedes, factores, CO2, REST ni frontend web).

> **Estado actual:** entorno (Dev Container + MariaDB + escritorio noVNC) construido y validado; la app muestra por ahora una **ventana de diagnóstico** de la conexión. Login y CRUD están **pendientes** (etapas E3–E4). Detalle y evidencias en [`ESTADO.md`](ESTADO.md).

No necesitas instalar Java, MariaDB ni Docker en tu equipo: todo corre en GitHub Codespaces.

---

## 1. Instalación inicial (una vez por Codespace)

1. Abre el repositorio en un Codespace (desde github.com o con VS Code + extensión *GitHub Codespaces*). Máquina recomendada: 2 núcleos / 8 GB.
2. Si el Codespace se creó antes de existir `.devcontainer/`, aplícalo: **Ctrl/Cmd+Shift+P → "Codespaces: Rebuild Container"**. El rebuild conserva `/workspaces` (código y `.devcontainer/.env`), pero reinicia procesos y el directorio home.
3. Durante la construcción ocurre automáticamente:
   - `initializeCommand` → `.devcontainer/init-secrets.sh` crea `.devcontainer/.env` con contraseñas aleatorias **solo si no existe** (ignorado por Git, permisos 600).
   - MariaDB arranca con un volumen persistente. **Solo si el volumen está vacío** se ejecuta `db/init/01-inicializar.sh`: crea `sg_huella_carbono_revision` (operativa) y `sg_huella_carbono_test` (pruebas), aplica `db/esquema/tablas.sql` y crea los usuarios `huella_app` (SELECT/INSERT/UPDATE) y `huella_test`.
   - `postStartCommand` → `.devcontainer/post-start.sh` aplica tu contraseña de escritorio y reinicia el servidor VNC.
4. Comprueba en la pestaña **Puertos** que el 6080 aparece con visibilidad **Private**. MariaDB (3306) no se reenvía ni se publica.

### Secretos

Todos están en `.devcontainer/.env` (ver `.devcontainer/env.example`). Para ver uno sin mostrar los demás:

```bash
grep '^HUELLA_VNC_PASSWORD=' .devcontainer/.env | cut -d= -f2
```

Cambia las contraseñas de BD **antes del primer arranque** de MariaDB (después ya están dentro del volumen). No subas `.env` a Git.

### Agente (opcional, solo desarrollo)

La imagen incluye la CLI de Claude Code; tras cada rebuild hay que volver a autenticarse ejecutando `claude` en la raíz. No copies archivos de autenticación al repositorio.

## 2. Arranque normal

1. Abre el escritorio: pestaña **Puertos → 6080 → icono del globo** (o abre `/vnc.html`), pulsa *Connect* e introduce `HUELLA_VNC_PASSWORD`.
2. En la terminal del Codespace:

```bash
scripts/ejecutar.sh          # compila si hace falta y abre la ventana en el escritorio
```

(`scripts/ejecutar.sh --demo` activará la cuenta demo cuando exista la etapa E3.)

Cerrar la ventana cierra el contexto Spring y el pool de conexiones y termina el proceso.

## 3. Pruebas

```bash
./mvnw -B test                         # unitarias (sin BD ni ventanas)
./mvnw -B verify -Dintegracion         # + integración contra MariaDB real (base sg_huella_carbono_test)
db/derivar-esquema.sh --verificar      # el esquema operativo coincide con el SQL de referencia
```

> Pendiente: las pruebas unitarias y de integración se añaden en E3–E4.

## 4. Respaldo

- `referencias/sg_huella_carbono_entrega.sql` se conserva **sin cambios** (SHA-256 `cd35e68b…a3`). Su sección 3 (copia SQL a `sg_huella_carbono_respaldo_revision_01`) es un procedimiento académico **manual**; nunca se ejecuta al arrancar.
- Los datos viven en el volumen Docker `huella-mariadb-data`: sobreviven a parar/reiniciar y a un rebuild del Codespace, pero **se pierden si eliminas el Codespace**. Exporta antes fuera del Codespace.

> Pendiente (E5): script documentado de exportación `mariadb-dump` y procedimiento de restauración probado.

## 5. Solución de problemas

| Síntoma | Causa probable / acción |
|---|---|
| El Codespace no arranca: `required variable ... is missing a value` | Falta `.devcontainer/.env` o una variable. Ejecuta `bash .devcontainer/init-secrets.sh` (no sobrescribe) y reconstruye. |
| noVNC pide contraseña y no la acepta | Usa `HUELLA_VNC_PASSWORD`; si cambiaste el `.env`, ejecuta `bash .devcontainer/post-start.sh`. |
| `No se pudo conectar con la base de datos` | `docker` no está dentro del contenedor; revisa el servicio `db` desde la vista de Codespaces o reconstruye. Nunca borres el volumen para "arreglarlo". |
| La ventana no aparece | Verifica `echo $DISPLAY` (= `:1`) y que el escritorio noVNC esté conectado. |

## Estructura

| Ruta | Contenido |
|---|---|
| `.devcontainer/` | Dev Container (Java 21, desktop-lite, MariaDB por compose) y scripts de secretos/arranque |
| `db/` | Derivación del esquema, esquema operativo e inicializador de MariaDB |
| `referencias/` | SQL de referencia original (no modificar) |
| `src/` | Código Java (paquete `edu.huella`) |
| `scripts/` | Arranque de la aplicación |
| `docs/` | README original del kit y evidencias |
| `AGENTS.md`, `CLAUDE.md`, `PLAN_IMPLEMENTACION.md`, `PROMPT_MAESTRO.md`, `PROMPTS_REVISION.md`, `ESTADO.md` | Contexto y estado para agentes |
