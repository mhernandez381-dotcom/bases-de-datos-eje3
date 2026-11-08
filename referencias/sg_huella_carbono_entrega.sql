-- ENTREGA DEL TALLER: SISTEMA DE GESTIÓN DE HUELLA DE CARBONO
-- Script unificado para MariaDB 10.4.32, ejecutable desde phpMyAdmin.
-- Contiene estructura corregida, copia lógica interna y verificaciones.
-- NO modifica sg_huella_carbono (base original).
-- Crea: sg_huella_carbono_revision
--       sg_huella_carbono_respaldo_revision_01
-- REQUISITO: ambas bases deben estar AUSENTES antes de comenzar.
-- Ejecutar una sola vez; detenerse ante cualquier error, no continuar.
-- No es idempotente: no utiliza DROP ni reemplaza bases existentes.
-- Sin datos de prueba: la ejecución completa produce tablas y respaldo VACÍOS.
-- Para demostrar copia de registros, agregar INSERT válidos en la sección 2
-- antes de ejecutar todo el archivo, o ejecutar por secciones y cargar datos
-- entre las secciones 1 y 3. No modificar datos durante la copia/verificación.
-- El DDL hace commit implícito; un ROLLBACK no deshace el script completo.
-- Después de verificar, exportar estructura y datos fuera del servidor.

-- COMPROBACIÓN INICIAL: debe devolver 0 filas. Si devuelve alguna, detenerse.
SELECT SCHEMA_NAME AS base_ya_existente
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME IN ('sg_huella_carbono_revision',
                      'sg_huella_carbono_respaldo_revision_01');

-- =====================================================================
-- SECCIÓN 1. CREACIÓN DEL MODELO RELACIONAL CORREGIDO
-- =====================================================================

-- ESTRUCTURA CORREGIDA / MariaDB 10.4.32 (versión del archivo recibido).
-- Instala una NUEVA base VACÍA. No modifica ni migra la base original.
-- No ejecutar si sg_huella_carbono_revision ya existe: detenerse ante errores.
-- CREATE/ALTER hacen commit implícito; el DDL no es una operación reversible.
-- DATE: YYYY-MM-DD; consumo.periodo: YYYY-MM-01.
-- TIMESTAMP: visualización YYYY-MM-DD HH:MM:SS; sesión configurada en UTC.
-- Borrado lógico: UPDATE tabla SET eliminado_en = CURRENT_TIMESTAMP WHERE id = ...;
-- Consultas activas: eliminado_en IS NULL; no usar DELETE para el borrado lógico.
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_VALUE_ON_ZERO,NO_ENGINE_SUBSTITUTION';
SET NAMES utf8mb4;
SET SESSION time_zone = '+00:00';
SET SESSION foreign_key_checks = 1;

CREATE DATABASE `sg_huella_carbono_revision` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `sg_huella_carbono_revision`;
-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `compensacion`
--

CREATE TABLE `compensacion` (
  `id` int(11) NOT NULL,
  `id_empresa` int(11) NOT NULL,
  `id_proyecto` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `cantidad_kg_co2e` decimal(14,3) NOT NULL,
  `comprobante` varchar(255) NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eliminado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `consumo`
--

CREATE TABLE `consumo` (
  `id` int(11) NOT NULL,
  `id_sede` int(11) NOT NULL,
  `id_factor` int(11) NOT NULL,
  `periodo` date NOT NULL,
  `cantidad` decimal(14,3) UNSIGNED NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eliminado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `empresa`
--

CREATE TABLE `empresa` (
  `id` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `nit` varchar(20) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eliminado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `factor_emision`
--

CREATE TABLE `factor_emision` (
  `id` int(11) NOT NULL,
  `id_fuente` int(11) NOT NULL,
  `valor` decimal(14,8) UNSIGNED NOT NULL,
  `unidad_consumo` varchar(20) NOT NULL,
  `anio` smallint(6) NOT NULL,
  `referencia` varchar(255) NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eliminado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `fuente_emision`
--

CREATE TABLE `fuente_emision` (
  `id` int(11) NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eliminado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `proyecto`
--

CREATE TABLE `proyecto` (
  `id` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eliminado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sede`
--

CREATE TABLE `sede` (
  `id` int(11) NOT NULL,
  `id_empresa` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(200) NOT NULL,
  `ciudad` varchar(100) NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eliminado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `correo` varchar(150) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `eliminado_en` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `compensacion`
--
ALTER TABLE `compensacion`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_empresa` (`id_empresa`),
  ADD KEY `id_proyecto` (`id_proyecto`);

--
-- Indices de la tabla `consumo`
--
ALTER TABLE `consumo`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_factor` (`id_factor`),
  ADD KEY `id_sede` (`id_sede`);

--
-- Indices de la tabla `empresa`
--
ALTER TABLE `empresa`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nit` (`nit`),
  ADD KEY `id_usuario` (`id_usuario`);

--
-- Indices de la tabla `factor_emision`
--
ALTER TABLE `factor_emision`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_fuente` (`id_fuente`);

--
-- Indices de la tabla `fuente_emision`
--
ALTER TABLE `fuente_emision`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `proyecto`
--
ALTER TABLE `proyecto`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `sede`
--
ALTER TABLE `sede`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_empresa` (`id_empresa`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `correo` (`correo`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `compensacion`
--
ALTER TABLE `compensacion`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `consumo`
--
ALTER TABLE `consumo`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `empresa`
--
ALTER TABLE `empresa`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `factor_emision`
--
ALTER TABLE `factor_emision`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `fuente_emision`
--
ALTER TABLE `fuente_emision`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `proyecto`
--
ALTER TABLE `proyecto`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `sede`
--
ALTER TABLE `sede`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `compensacion`
--
ALTER TABLE `compensacion`
  ADD CONSTRAINT `compensacion_ibfk_1` FOREIGN KEY (`id_empresa`) REFERENCES `empresa` (`id`),
  ADD CONSTRAINT `compensacion_ibfk_2` FOREIGN KEY (`id_proyecto`) REFERENCES `proyecto` (`id`);

--
-- Filtros para la tabla `consumo`
--
ALTER TABLE `consumo`
  ADD CONSTRAINT `consumo_ibfk_1` FOREIGN KEY (`id_factor`) REFERENCES `factor_emision` (`id`),
  ADD CONSTRAINT `consumo_ibfk_2` FOREIGN KEY (`id_sede`) REFERENCES `sede` (`id`);

--
-- Filtros para la tabla `empresa`
--
ALTER TABLE `empresa`
  ADD CONSTRAINT `empresa_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`);

--
-- Filtros para la tabla `factor_emision`
--
ALTER TABLE `factor_emision`
  ADD CONSTRAINT `factor_emision_ibfk_1` FOREIGN KEY (`id_fuente`) REFERENCES `fuente_emision` (`id`);

--
-- Filtros para la tabla `sede`
--
ALTER TABLE `sede`
  ADD CONSTRAINT `sede_ibfk_1` FOREIGN KEY (`id_empresa`) REFERENCES `empresa` (`id`);

-- Reglas mínimas de negocio mediante CHECK, para MariaDB 10.4.
ALTER TABLE `factor_emision`
  ADD CONSTRAINT `chk_factor_valor` CHECK (`valor` >= 0),
  ADD CONSTRAINT `chk_factor_anio` CHECK (`anio` BETWEEN 1900 AND 9999);
ALTER TABLE `consumo`
  ADD CONSTRAINT `chk_consumo_cantidad` CHECK (`cantidad` > 0),
  ADD CONSTRAINT `chk_consumo_periodo` CHECK (DAYOFMONTH(`periodo`) = 1);
ALTER TABLE `compensacion`
  ADD CONSTRAINT `chk_compensacion_cantidad` CHECK (`cantidad_kg_co2e` > 0);

-- Cálculo conceptual: kg CO2e = consumo.cantidad * factor_emision.valor.
-- La aplicación valida unidades, vigencia del factor y propiedad de la empresa.
-- No se incluyen datos ficticios ni se inventan fechas para registros antiguos.

-- =====================================================================
-- SECCIÓN 2. DATOS DE PRUEBA (OPCIONAL; NO INCLUIDOS)
-- =====================================================================
-- Agregar aquí INSERT con datos ficticios si se desea demostrar copia de datos.
-- Orden compatible con las FK:
-- usuario -> empresa -> sede; fuente_emision -> factor_emision;
-- consumo (requiere sede y factor_emision); proyecto -> compensacion
-- (compensacion también requiere empresa).
-- No usar información personal ni contraseñas reales en evidencias del taller.
-- No se inventan factores de emisión ni se presentan datos ficticios como reales.

-- =====================================================================
-- SECCIÓN 3. COPIA DE SEGURIDAD MEDIANTE SENTENCIAS SQL Y VERIFICACIÓN
-- =====================================================================

-- RESPALDO ACADÉMICO MEDIANTE SENTENCIAS SQL / MariaDB 10.4.32.
-- Origen: sg_huella_carbono_revision
-- Destino NUEVO: sg_huella_carbono_respaldo_revision_01
-- Ejecutar por BLOQUES desde phpMyAdmin. NO continuar si un bloque falla.
-- Condiciones: origen existente, ocho tablas InnoDB del esquema esperado,
-- sin escrituras ni cambios de estructura desde antes de iniciar hasta verificar.
-- Conserva todos los registros, incluidos los eliminados lógicamente.
-- NO es exportación a disco ni backup de cuentas, permisos, triggers o rutinas.
-- No repetir sobre el mismo destino ni utilizar INSERT IGNORE/REPLACE.

-- BLOQUE 1: comprobación. Confirmar versión, ocho tablas y motor InnoDB.
SELECT VERSION() AS version_servidor;
SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'sg_huella_carbono_revision' AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
-- Debe devolver 0: si el destino existe, detenerse y elegir otro nombre.
SELECT COUNT(*) AS destino_ya_existe FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'sg_huella_carbono_respaldo_revision_01';

-- BLOQUE 2: estructura. El DDL produce commits implícitos.
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_VALUE_ON_ZERO,NO_ENGINE_SUBSTITUTION';
SET NAMES utf8mb4;
SET SESSION time_zone = '+00:00';
SET SESSION foreign_key_checks = 1;

CREATE DATABASE `sg_huella_carbono_respaldo_revision_01` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE TABLE `sg_huella_carbono_respaldo_revision_01`.`usuario` LIKE `sg_huella_carbono_revision`.`usuario`;
CREATE TABLE `sg_huella_carbono_respaldo_revision_01`.`empresa` LIKE `sg_huella_carbono_revision`.`empresa`;
CREATE TABLE `sg_huella_carbono_respaldo_revision_01`.`sede` LIKE `sg_huella_carbono_revision`.`sede`;
CREATE TABLE `sg_huella_carbono_respaldo_revision_01`.`fuente_emision` LIKE `sg_huella_carbono_revision`.`fuente_emision`;
CREATE TABLE `sg_huella_carbono_respaldo_revision_01`.`factor_emision` LIKE `sg_huella_carbono_revision`.`factor_emision`;
CREATE TABLE `sg_huella_carbono_respaldo_revision_01`.`consumo` LIKE `sg_huella_carbono_revision`.`consumo`;
CREATE TABLE `sg_huella_carbono_respaldo_revision_01`.`proyecto` LIKE `sg_huella_carbono_revision`.`proyecto`;
CREATE TABLE `sg_huella_carbono_respaldo_revision_01`.`compensacion` LIKE `sg_huella_carbono_revision`.`compensacion`;

-- LIKE copia columnas e índices, pero NO las claves foráneas. Se recrean
-- apuntando exclusivamente a las tablas de la base de respaldo.
ALTER TABLE `sg_huella_carbono_respaldo_revision_01`.`empresa`
  ADD CONSTRAINT `fk_empresa_id_usuario` FOREIGN KEY (`id_usuario`)
  REFERENCES `sg_huella_carbono_respaldo_revision_01`.`usuario` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `sg_huella_carbono_respaldo_revision_01`.`sede`
  ADD CONSTRAINT `fk_sede_id_empresa` FOREIGN KEY (`id_empresa`)
  REFERENCES `sg_huella_carbono_respaldo_revision_01`.`empresa` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `sg_huella_carbono_respaldo_revision_01`.`factor_emision`
  ADD CONSTRAINT `fk_factor_emision_id_fuente` FOREIGN KEY (`id_fuente`)
  REFERENCES `sg_huella_carbono_respaldo_revision_01`.`fuente_emision` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `sg_huella_carbono_respaldo_revision_01`.`consumo`
  ADD CONSTRAINT `fk_consumo_id_factor` FOREIGN KEY (`id_factor`)
  REFERENCES `sg_huella_carbono_respaldo_revision_01`.`factor_emision` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `sg_huella_carbono_respaldo_revision_01`.`consumo`
  ADD CONSTRAINT `fk_consumo_id_sede` FOREIGN KEY (`id_sede`)
  REFERENCES `sg_huella_carbono_respaldo_revision_01`.`sede` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `sg_huella_carbono_respaldo_revision_01`.`compensacion`
  ADD CONSTRAINT `fk_compensacion_id_empresa` FOREIGN KEY (`id_empresa`)
  REFERENCES `sg_huella_carbono_respaldo_revision_01`.`empresa` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `sg_huella_carbono_respaldo_revision_01`.`compensacion`
  ADD CONSTRAINT `fk_compensacion_id_proyecto` FOREIGN KEY (`id_proyecto`)
  REFERENCES `sg_huella_carbono_respaldo_revision_01`.`proyecto` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- BLOQUE 3: datos. Ejecutar completo, sólo si el bloque 2 terminó sin errores.
-- No agregar DDL dentro de esta transacción. Mantener la aplicación sin escrituras.
-- Si falla un INSERT, detener la ejecución y hacer ROLLBACK en la misma sesión;
-- NO continuar hasta COMMIT ni reutilizar una copia parcial como respaldo válido.
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_VALUE_ON_ZERO,NO_ENGINE_SUBSTITUTION';
SET NAMES utf8mb4;
SET SESSION time_zone = '+00:00';
SET SESSION foreign_key_checks = 1;

START TRANSACTION;

INSERT INTO `sg_huella_carbono_respaldo_revision_01`.`usuario` (`id`, `nombre`, `correo`, `password_hash`, `creado_en`, `actualizado_en`, `eliminado_en`)
SELECT `id`, `nombre`, `correo`, `password_hash`, `creado_en`, `actualizado_en`, `eliminado_en`
FROM `sg_huella_carbono_revision`.`usuario`;

INSERT INTO `sg_huella_carbono_respaldo_revision_01`.`empresa` (`id`, `id_usuario`, `nit`, `nombre`, `creado_en`, `actualizado_en`, `eliminado_en`)
SELECT `id`, `id_usuario`, `nit`, `nombre`, `creado_en`, `actualizado_en`, `eliminado_en`
FROM `sg_huella_carbono_revision`.`empresa`;

INSERT INTO `sg_huella_carbono_respaldo_revision_01`.`sede` (`id`, `id_empresa`, `nombre`, `direccion`, `ciudad`, `creado_en`, `actualizado_en`, `eliminado_en`)
SELECT `id`, `id_empresa`, `nombre`, `direccion`, `ciudad`, `creado_en`, `actualizado_en`, `eliminado_en`
FROM `sg_huella_carbono_revision`.`sede`;

INSERT INTO `sg_huella_carbono_respaldo_revision_01`.`fuente_emision` (`id`, `nombre`, `creado_en`, `actualizado_en`, `eliminado_en`)
SELECT `id`, `nombre`, `creado_en`, `actualizado_en`, `eliminado_en`
FROM `sg_huella_carbono_revision`.`fuente_emision`;

INSERT INTO `sg_huella_carbono_respaldo_revision_01`.`factor_emision` (`id`, `id_fuente`, `valor`, `unidad_consumo`, `anio`, `referencia`, `creado_en`, `actualizado_en`, `eliminado_en`)
SELECT `id`, `id_fuente`, `valor`, `unidad_consumo`, `anio`, `referencia`, `creado_en`, `actualizado_en`, `eliminado_en`
FROM `sg_huella_carbono_revision`.`factor_emision`;

INSERT INTO `sg_huella_carbono_respaldo_revision_01`.`consumo` (`id`, `id_sede`, `id_factor`, `periodo`, `cantidad`, `creado_en`, `actualizado_en`, `eliminado_en`)
SELECT `id`, `id_sede`, `id_factor`, `periodo`, `cantidad`, `creado_en`, `actualizado_en`, `eliminado_en`
FROM `sg_huella_carbono_revision`.`consumo`;

INSERT INTO `sg_huella_carbono_respaldo_revision_01`.`proyecto` (`id`, `nombre`, `descripcion`, `creado_en`, `actualizado_en`, `eliminado_en`)
SELECT `id`, `nombre`, `descripcion`, `creado_en`, `actualizado_en`, `eliminado_en`
FROM `sg_huella_carbono_revision`.`proyecto`;

INSERT INTO `sg_huella_carbono_respaldo_revision_01`.`compensacion` (`id`, `id_empresa`, `id_proyecto`, `fecha`, `cantidad_kg_co2e`, `comprobante`, `creado_en`, `actualizado_en`, `eliminado_en`)
SELECT `id`, `id_empresa`, `id_proyecto`, `fecha`, `cantidad_kg_co2e`, `comprobante`, `creado_en`, `actualizado_en`, `eliminado_en`
FROM `sg_huella_carbono_revision`.`compensacion`;

COMMIT;

-- BLOQUE 4: verificación, con el origen todavía sin escrituras.
-- Cada tabla debe tener igual número de filas en origen y copia.
SELECT 'usuario' AS tabla,
  (SELECT COUNT(*) FROM `sg_huella_carbono_revision`.`usuario`) AS filas_origen,
  (SELECT COUNT(*) FROM `sg_huella_carbono_respaldo_revision_01`.`usuario`) AS filas_respaldo
UNION ALL
SELECT 'empresa' AS tabla,
  (SELECT COUNT(*) FROM `sg_huella_carbono_revision`.`empresa`) AS filas_origen,
  (SELECT COUNT(*) FROM `sg_huella_carbono_respaldo_revision_01`.`empresa`) AS filas_respaldo
UNION ALL
SELECT 'sede' AS tabla,
  (SELECT COUNT(*) FROM `sg_huella_carbono_revision`.`sede`) AS filas_origen,
  (SELECT COUNT(*) FROM `sg_huella_carbono_respaldo_revision_01`.`sede`) AS filas_respaldo
UNION ALL
SELECT 'fuente_emision' AS tabla,
  (SELECT COUNT(*) FROM `sg_huella_carbono_revision`.`fuente_emision`) AS filas_origen,
  (SELECT COUNT(*) FROM `sg_huella_carbono_respaldo_revision_01`.`fuente_emision`) AS filas_respaldo
UNION ALL
SELECT 'factor_emision' AS tabla,
  (SELECT COUNT(*) FROM `sg_huella_carbono_revision`.`factor_emision`) AS filas_origen,
  (SELECT COUNT(*) FROM `sg_huella_carbono_respaldo_revision_01`.`factor_emision`) AS filas_respaldo
UNION ALL
SELECT 'consumo' AS tabla,
  (SELECT COUNT(*) FROM `sg_huella_carbono_revision`.`consumo`) AS filas_origen,
  (SELECT COUNT(*) FROM `sg_huella_carbono_respaldo_revision_01`.`consumo`) AS filas_respaldo
UNION ALL
SELECT 'proyecto' AS tabla,
  (SELECT COUNT(*) FROM `sg_huella_carbono_revision`.`proyecto`) AS filas_origen,
  (SELECT COUNT(*) FROM `sg_huella_carbono_respaldo_revision_01`.`proyecto`) AS filas_respaldo
UNION ALL
SELECT 'compensacion' AS tabla,
  (SELECT COUNT(*) FROM `sg_huella_carbono_revision`.`compensacion`) AS filas_origen,
  (SELECT COUNT(*) FROM `sg_huella_carbono_respaldo_revision_01`.`compensacion`) AS filas_respaldo;

-- Además del conteo, comparar todos los campos por id (NULL seguro).
-- CAST AS BINARY detecta diferencias de mayúsculas y acentos en textos.
-- Todos los resultados deben ser 0.
SELECT 'usuario' AS tabla, COUNT(*) AS filas_faltantes_o_distintas
FROM `sg_huella_carbono_revision`.`usuario` o
LEFT JOIN `sg_huella_carbono_respaldo_revision_01`.`usuario` r ON r.`id` = o.`id`
WHERE r.`id` IS NULL OR NOT (
    (CAST(o.`id` AS BINARY) <=> CAST(r.`id` AS BINARY))
    AND (CAST(o.`nombre` AS BINARY) <=> CAST(r.`nombre` AS BINARY))
    AND (CAST(o.`correo` AS BINARY) <=> CAST(r.`correo` AS BINARY))
    AND (CAST(o.`password_hash` AS BINARY) <=> CAST(r.`password_hash` AS BINARY))
    AND (CAST(o.`creado_en` AS BINARY) <=> CAST(r.`creado_en` AS BINARY))
    AND (CAST(o.`actualizado_en` AS BINARY) <=> CAST(r.`actualizado_en` AS BINARY))
    AND (CAST(o.`eliminado_en` AS BINARY) <=> CAST(r.`eliminado_en` AS BINARY))
)
UNION ALL
SELECT 'empresa' AS tabla, COUNT(*) AS filas_faltantes_o_distintas
FROM `sg_huella_carbono_revision`.`empresa` o
LEFT JOIN `sg_huella_carbono_respaldo_revision_01`.`empresa` r ON r.`id` = o.`id`
WHERE r.`id` IS NULL OR NOT (
    (CAST(o.`id` AS BINARY) <=> CAST(r.`id` AS BINARY))
    AND (CAST(o.`id_usuario` AS BINARY) <=> CAST(r.`id_usuario` AS BINARY))
    AND (CAST(o.`nit` AS BINARY) <=> CAST(r.`nit` AS BINARY))
    AND (CAST(o.`nombre` AS BINARY) <=> CAST(r.`nombre` AS BINARY))
    AND (CAST(o.`creado_en` AS BINARY) <=> CAST(r.`creado_en` AS BINARY))
    AND (CAST(o.`actualizado_en` AS BINARY) <=> CAST(r.`actualizado_en` AS BINARY))
    AND (CAST(o.`eliminado_en` AS BINARY) <=> CAST(r.`eliminado_en` AS BINARY))
)
UNION ALL
SELECT 'sede' AS tabla, COUNT(*) AS filas_faltantes_o_distintas
FROM `sg_huella_carbono_revision`.`sede` o
LEFT JOIN `sg_huella_carbono_respaldo_revision_01`.`sede` r ON r.`id` = o.`id`
WHERE r.`id` IS NULL OR NOT (
    (CAST(o.`id` AS BINARY) <=> CAST(r.`id` AS BINARY))
    AND (CAST(o.`id_empresa` AS BINARY) <=> CAST(r.`id_empresa` AS BINARY))
    AND (CAST(o.`nombre` AS BINARY) <=> CAST(r.`nombre` AS BINARY))
    AND (CAST(o.`direccion` AS BINARY) <=> CAST(r.`direccion` AS BINARY))
    AND (CAST(o.`ciudad` AS BINARY) <=> CAST(r.`ciudad` AS BINARY))
    AND (CAST(o.`creado_en` AS BINARY) <=> CAST(r.`creado_en` AS BINARY))
    AND (CAST(o.`actualizado_en` AS BINARY) <=> CAST(r.`actualizado_en` AS BINARY))
    AND (CAST(o.`eliminado_en` AS BINARY) <=> CAST(r.`eliminado_en` AS BINARY))
)
UNION ALL
SELECT 'fuente_emision' AS tabla, COUNT(*) AS filas_faltantes_o_distintas
FROM `sg_huella_carbono_revision`.`fuente_emision` o
LEFT JOIN `sg_huella_carbono_respaldo_revision_01`.`fuente_emision` r ON r.`id` = o.`id`
WHERE r.`id` IS NULL OR NOT (
    (CAST(o.`id` AS BINARY) <=> CAST(r.`id` AS BINARY))
    AND (CAST(o.`nombre` AS BINARY) <=> CAST(r.`nombre` AS BINARY))
    AND (CAST(o.`creado_en` AS BINARY) <=> CAST(r.`creado_en` AS BINARY))
    AND (CAST(o.`actualizado_en` AS BINARY) <=> CAST(r.`actualizado_en` AS BINARY))
    AND (CAST(o.`eliminado_en` AS BINARY) <=> CAST(r.`eliminado_en` AS BINARY))
)
UNION ALL
SELECT 'factor_emision' AS tabla, COUNT(*) AS filas_faltantes_o_distintas
FROM `sg_huella_carbono_revision`.`factor_emision` o
LEFT JOIN `sg_huella_carbono_respaldo_revision_01`.`factor_emision` r ON r.`id` = o.`id`
WHERE r.`id` IS NULL OR NOT (
    (CAST(o.`id` AS BINARY) <=> CAST(r.`id` AS BINARY))
    AND (CAST(o.`id_fuente` AS BINARY) <=> CAST(r.`id_fuente` AS BINARY))
    AND (CAST(o.`valor` AS BINARY) <=> CAST(r.`valor` AS BINARY))
    AND (CAST(o.`unidad_consumo` AS BINARY) <=> CAST(r.`unidad_consumo` AS BINARY))
    AND (CAST(o.`anio` AS BINARY) <=> CAST(r.`anio` AS BINARY))
    AND (CAST(o.`referencia` AS BINARY) <=> CAST(r.`referencia` AS BINARY))
    AND (CAST(o.`creado_en` AS BINARY) <=> CAST(r.`creado_en` AS BINARY))
    AND (CAST(o.`actualizado_en` AS BINARY) <=> CAST(r.`actualizado_en` AS BINARY))
    AND (CAST(o.`eliminado_en` AS BINARY) <=> CAST(r.`eliminado_en` AS BINARY))
)
UNION ALL
SELECT 'consumo' AS tabla, COUNT(*) AS filas_faltantes_o_distintas
FROM `sg_huella_carbono_revision`.`consumo` o
LEFT JOIN `sg_huella_carbono_respaldo_revision_01`.`consumo` r ON r.`id` = o.`id`
WHERE r.`id` IS NULL OR NOT (
    (CAST(o.`id` AS BINARY) <=> CAST(r.`id` AS BINARY))
    AND (CAST(o.`id_sede` AS BINARY) <=> CAST(r.`id_sede` AS BINARY))
    AND (CAST(o.`id_factor` AS BINARY) <=> CAST(r.`id_factor` AS BINARY))
    AND (CAST(o.`periodo` AS BINARY) <=> CAST(r.`periodo` AS BINARY))
    AND (CAST(o.`cantidad` AS BINARY) <=> CAST(r.`cantidad` AS BINARY))
    AND (CAST(o.`creado_en` AS BINARY) <=> CAST(r.`creado_en` AS BINARY))
    AND (CAST(o.`actualizado_en` AS BINARY) <=> CAST(r.`actualizado_en` AS BINARY))
    AND (CAST(o.`eliminado_en` AS BINARY) <=> CAST(r.`eliminado_en` AS BINARY))
)
UNION ALL
SELECT 'proyecto' AS tabla, COUNT(*) AS filas_faltantes_o_distintas
FROM `sg_huella_carbono_revision`.`proyecto` o
LEFT JOIN `sg_huella_carbono_respaldo_revision_01`.`proyecto` r ON r.`id` = o.`id`
WHERE r.`id` IS NULL OR NOT (
    (CAST(o.`id` AS BINARY) <=> CAST(r.`id` AS BINARY))
    AND (CAST(o.`nombre` AS BINARY) <=> CAST(r.`nombre` AS BINARY))
    AND (CAST(o.`descripcion` AS BINARY) <=> CAST(r.`descripcion` AS BINARY))
    AND (CAST(o.`creado_en` AS BINARY) <=> CAST(r.`creado_en` AS BINARY))
    AND (CAST(o.`actualizado_en` AS BINARY) <=> CAST(r.`actualizado_en` AS BINARY))
    AND (CAST(o.`eliminado_en` AS BINARY) <=> CAST(r.`eliminado_en` AS BINARY))
)
UNION ALL
SELECT 'compensacion' AS tabla, COUNT(*) AS filas_faltantes_o_distintas
FROM `sg_huella_carbono_revision`.`compensacion` o
LEFT JOIN `sg_huella_carbono_respaldo_revision_01`.`compensacion` r ON r.`id` = o.`id`
WHERE r.`id` IS NULL OR NOT (
    (CAST(o.`id` AS BINARY) <=> CAST(r.`id` AS BINARY))
    AND (CAST(o.`id_empresa` AS BINARY) <=> CAST(r.`id_empresa` AS BINARY))
    AND (CAST(o.`id_proyecto` AS BINARY) <=> CAST(r.`id_proyecto` AS BINARY))
    AND (CAST(o.`fecha` AS BINARY) <=> CAST(r.`fecha` AS BINARY))
    AND (CAST(o.`cantidad_kg_co2e` AS BINARY) <=> CAST(r.`cantidad_kg_co2e` AS BINARY))
    AND (CAST(o.`comprobante` AS BINARY) <=> CAST(r.`comprobante` AS BINARY))
    AND (CAST(o.`creado_en` AS BINARY) <=> CAST(r.`creado_en` AS BINARY))
    AND (CAST(o.`actualizado_en` AS BINARY) <=> CAST(r.`actualizado_en` AS BINARY))
    AND (CAST(o.`eliminado_en` AS BINARY) <=> CAST(r.`eliminado_en` AS BINARY))
);

-- Debe mostrar siete relaciones, todas con esquema_referenciado = destino.
SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_SCHEMA AS esquema_referenciado,
       REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'sg_huella_carbono_respaldo_revision_01' AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;
-- Guardar capturas de estos resultados como evidencia, sin exponer contraseñas.
