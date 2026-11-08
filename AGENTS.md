# Instrucciones del proyecto

- Aplicación universitaria pequeña: **Java + Spring Boot no web + Swing + Spring JDBC + MariaDB**. Este stack específico prevalece sobre preferencias genéricas de Angular/Strapi para otros proyectos.
- Leer `PLAN_IMPLEMENTACION.md`, `ESTADO.md` y el SQL de `referencias/` antes de implementar. El SQL es material de referencia, no un script autorizado para ejecutar indiscriminadamente.
- Un usuario semilla, login y un único CRUD de Empresa. Sin roles, registro de usuarios, REST, frontend web, cálculo CO2 ni pantallas de otras entidades.
- Separación: vista → controlador → servicio → DAO; DTO y modelo con tipos concretos. No SQL en la vista ni responsabilidades de interfaz en el DTO.
- Un agente escritor por directorio. No lanzar otros agentes o CLIs automáticamente. La revisión independiente es secuencial.
- No instalar Java, bases de datos o Docker en el equipo local. Si el entorno es local, preparar archivos y registrar la validación remota pendiente.
- En remoto, respetar permisos y políticas del entorno. Autenticaciones, creación de recursos, costes, publicación, cambios globales y destrucción de datos requieren autorización específica.
- No crear repositorios ni hacer push, merge o force-push por inferencia. Proponer checkpoints y hacer commits solo cuando el usuario lo autorice.
- No guardar secretos, contraseñas en claro, archivos de autenticación, bases de datos ni logs sensibles en Git.
- Preservar el SQL de referencia. Derivar inicialización separada y explícita; nunca ejecutar el respaldo completo al iniciar la aplicación.
- SQL parametrizado, NIT único, contraseña hash, propietario desde sesión, borrado lógico y consultas de activos.
- Swing en EDT; acceso a BD y hashing fuera del EDT. No usar pruebas de navegador para afirmar que se verificó una ventana Swing.
- Fijar versiones compatibles tras consultar documentación oficial. No usar `latest`, snapshots ni APIs inventadas.
- Probar con MariaDB real para acreditar SQL; no sustituir silenciosamente por H2.
- Actualizar `ESTADO.md` al cerrar cada etapa o antes de un rebuild/traspaso. Distinguir implementado, probado, fallido y pendiente.
- Comunicar en español: resumen, cambios, evidencia, riesgos y siguiente paso. No afirmar «funciona» sin pruebas ejecutadas.
