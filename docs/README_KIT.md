# Kit de implementación — Huella de carbono: módulo Empresas

Estado: **plan y contexto para un agente; todavía no es una aplicación ejecutable**.
Preparado el 8 de noviembre de 2026. No crea repositorios, Codespaces ni recursos de pago.

## Decisión práctica

Usar **VS Code local conectado a GitHub Codespaces**, y ejecutar **un agente CLI dentro del Codespace**. El editor se ve en tu equipo, pero el código, Java, MariaDB y los comandos están en el remoto. No hace falta instalar el stack Java localmente.

- Claude Code implementa por etapas. Puedes elegir otro agente leyendo explícitamente los mismos documentos.
- Copilot o Codex pueden revisar un cambio terminado, de forma secuencial y preferentemente sin editar.
- Evitar, para este CRUD, una cadena Copilot → Claude → Codex: añade coordinación sin mejorar necesariamente el resultado.
- Un único directorio de trabajo activo evita conflictos. Git conserva y comparte commits; no sincroniza archivos sin commit de forma automática.

**Agente principal elegido por el usuario: Claude Code.** Codex o Copilot quedan como revisores opcionales; no se necesita Codex Cloud.

## Contenido

| Archivo | Función |
|---|---|
| `CLAUDE.md` | Entrada de instrucciones para Claude Code |
| `AGENTS.md` | Reglas breves del proyecto |
| `PLAN_IMPLEMENTACION.md` | Alcance, arquitectura, etapas y aceptación |
| `PROMPT_MAESTRO.md` | Encargo completo para el implementador |
| `ESTADO.md` | Traspaso de contexto entre sesiones y agentes |
| `PROMPTS_REVISION.md` | Revisión independiente y reanudación |
| `referencias/sg_huella_carbono_entrega.sql` | Copia sin cambios del SQL aportado |

## Arranque recomendado, sin ciclo local/remoto en cada cambio

1. Crear, con tu aprobación, un repositorio privado dedicado al MVP. No subir toda la carpeta de trabajos universitarios.
2. Copiar **el contenido de este kit** a la raíz de ese repositorio. Si ya hay archivos, revisarlos y combinar instrucciones, no sobrescribirlos.
3. Subir los documentos y el SQL mediante Git o la interfaz de GitHub. Antes de subir, revisar que no haya credenciales o datos reales.
4. Crear un Codespace desde ese repositorio. Un Codespace inicial puede usar la configuración predeterminada; el agente preparará después la configuración reproducible de Java, escritorio y BD.
5. Abrirlo con tu VS Code mediante la extensión GitHub Codespaces, o permanecer en el navegador. Confirmar en la interfaz que es una conexión remota.
6. En la **terminal del Codespace**, verificar `pwd`, `git status` y `command -v claude`. El Claude Code instalado en tu equipo no se hereda automáticamente.
7. Si falta la CLI remota, instalarla con el procedimiento oficial vigente y la autorización correspondiente. Autenticar esa instalación; no subir archivos de autenticación a Git.
8. Desde la raíz del repositorio, iniciar `claude` y pegar el contenido de `PROMPT_MAESTRO.md`. Otra opción, en Bash/Zsh, es `claude` y pegar el prompt dentro de la sesión.
9. El agente primero prepara y verifica el entorno. Si hace falta reconstruir el contenedor, guarda el estado, solicita la acción y después continúa desde `ESTADO.md`. No asumir que su proceso o autenticación sobreviven al rebuild.
10. Tras la prueba de ventana Swing + conexión MariaDB, continuar con login y CRUD. Git se usa para checkpoints y entrega, no para transportar cada edición hacia otro equipo.

### Autenticación de Claude Code en el remoto

Ejecutar `claude` desde la raíz del repositorio y seguir su inicio de sesión oficial. La instalación y autenticación locales no se heredan automáticamente. Verificar disponibilidad con `claude --version`. No copiar tokens, archivos de autenticación ni claves al repositorio. El acceso y consumo dependen del método de autenticación y de tu cuenta; no asumir que la cuota de Codespaces incluye Claude Code.

Consulta la [guía oficial de Claude Code](https://code.claude.com/docs/en/quickstart) para instalación e inicio de sesión. No se instaló ni autenticó ninguna CLI al adaptar este kit.

## Alternativa: generar desde este chat o desde la CLI local

También es posible. El agente local escribe archivos sin necesitar Java para la edición. **Eso no prueba que compilen o funcionen**.

Flujo: editar local → revisar diff → commit/push autorizado → en Codespaces obtener la misma rama con pull → compilar/probar → registrar resultados. Si se corrige en remoto, primero sincronizar esos commits antes de volver a editar local. No editar las dos copias a la vez ni usar force-push para resolver divergencias.

Este chat puede preparar los archivos locales; no se conecta automáticamente al Codespace por el hecho de que lo abras. Para ejecutar comandos remotos desde un agente local haría falta una conexión explícita autorizada, por ejemplo SSH. No la necesitamos en el flujo recomendado.

## Qué significa «un prompt»

Un encargo inicial con contexto completo, etapas, pruebas y límites. No una garantía de implementación sin supervisión ni autenticación. El agente debe avanzar por etapas verificadas y detenerse solo ante decisiones relevantes, permisos, credenciales, costes o validaciones que no pueda ejecutar. Puede reanudarse sin volver a explicar todo usando `ESTADO.md`.

## Documentación oficial consultada

- [Claude Code: inicio](https://code.claude.com/docs/en/quickstart)
- [Claude Code: instrucciones de proyecto](https://code.claude.com/docs/en/memory)
- [Codespaces desde VS Code](https://docs.github.com/en/codespaces/developing-in-a-codespace/using-github-codespaces-in-visual-studio-code)
- [Entorno Java en Codespaces](https://docs.github.com/en/codespaces/setting-up-your-project-for-codespaces/adding-a-dev-container-configuration/setting-up-your-java-project-for-codespaces)
- [Escritorio remoto desktop-lite](https://github.com/devcontainers/features/tree/main/src/desktop-lite)
- [SpringApplicationBuilder](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/builder/SpringApplicationBuilder.html)

La capacidad general está documentada. **Este kit no acredita una prueba real en tu Codespace.**
