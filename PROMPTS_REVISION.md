# Prompts auxiliares

## Revisión independiente: Copilot, Claude Code o Codex

Usar cuando el implementador termine un hito. Ejecutar secuencialmente sobre la misma revisión de código; no pedir a dos agentes que editen a la vez.

```text
Actúa como revisor técnico de este MVP Java de escritorio. Lee AGENTS.md,
PLAN_IMPLEMENTACION.md y ESTADO.md, y revisa el diff del hito que acabamos
de implementar. Confirma la rama y base de comparación; si no existen,
limita la revisión a los archivos indicados y declara esa limitación.

No edites archivos ni instales dependencias. No lances otros agentes.
Puedes ejecutar pruebas existentes y no destructivas en el entorno
permitido; no uses la base de datos real/demo para pruebas destructivas.

Busca errores concretos: incumplimiento de alcance, SQL concatenado,
credenciales expuestas, hash/sesión inseguros, seed destructiva, acceso JDBC
en EDT, falta de filtros de propietario/activos, UNIQUE mal gestionado,
borrado físico, timestamps incorrectos y resultados falsos en las pruebas.
Comprueba también arranque no web, cierre de recursos y reproducibilidad.

Reporta hallazgos con severidad, archivo/línea, escenario reproducible e
impacto. Separa defectos confirmados de riesgos pendientes de ejecutar.
No pidas refactorizaciones cosméticas ni infraestructura fuera del MVP.
Termina con pruebas ejecutadas y validaciones pendientes. Si no encuentras
defectos, dilo sin declarar automáticamente que toda la aplicación funciona.
```

## Reanudar después de cambiar de agente o reconstruir Codespaces

```text
Continúa la implementación existente. Lee AGENTS.md, PLAN_IMPLEMENTACION.md,
PROMPT_MAESTRO.md y ESTADO.md. Inspecciona el repositorio y entorno actuales;
no supongas que procesos, credenciales o versiones sobrevivieron al rebuild.
No regeneres ni borres lo que ya existe. Contrasta las evidencias guardadas
con el estado actual y retoma la primera tarea pendiente ejecutable.
Mantén el alcance login + CRUD Empresa y todas las restricciones del plan.
```

## Preparar desde un agente local sin instalar Java

```text
Lee el kit y trabaja en modo preparación local. No instales Java, Maven,
MariaDB, Docker ni otras herramientas globales. Genera los archivos de E1
que puedan prepararse estáticamente y documenta cómo validarlos en Codespaces.
No marques compilación, GUI o integración como aprobadas. No publiques ni
crees recursos remotos sin autorización. Deja ESTADO.md listo para el relevo.
```
