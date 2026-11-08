# shellcheck shell=bash
# Inicialización de MariaDB para el MVP. Lo ejecuta (con "source") el entrypoint
# oficial de la imagen mariadb ÚNICAMENTE cuando el directorio de datos está
# vacío (primer arranque del volumen). No hace DROP ni sobrescribe nada.
#
# Crea:
#  - sg_huella_carbono_revision : base operativa, esquema = db/esquema/tablas.sql
#  - sg_huella_carbono_test     : base separada para pruebas de integración
#  - usuario app  : SELECT, INSERT, UPDATE sobre la operativa (sin DELETE:
#                   el borrado es lógico; sin DDL)
#  - usuario test : ALL sobre la base de pruebas solamente

# Sin "set -u": este archivo se carga dentro del entrypoint oficial (ya usa -eo pipefail).

_huella_validar() {
  local nombre="$1" valor="$2"
  if [[ ! "$valor" =~ ^[A-Za-z0-9._-]{8,64}$ ]]; then
    echo "huella-init: $nombre ausente o con caracteres no admitidos ([A-Za-z0-9._-], 8-64)." >&2
    exit 1
  fi
}
_huella_validar HUELLA_DB_APP_USER "${HUELLA_DB_APP_USER:-}"
_huella_validar HUELLA_DB_APP_PASSWORD "${HUELLA_DB_APP_PASSWORD:-}"
_huella_validar HUELLA_DB_TEST_USER "${HUELLA_DB_TEST_USER:-}"
_huella_validar HUELLA_DB_TEST_PASSWORD "${HUELLA_DB_TEST_PASSWORD:-}"

echo "huella-init: creando bases de datos."
docker_process_sql <<SQL
CREATE DATABASE \`sg_huella_carbono_revision\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE \`sg_huella_carbono_test\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
SQL

echo "huella-init: aplicando esquema derivado a sg_huella_carbono_revision."
docker_process_sql --database=sg_huella_carbono_revision < /huella-esquema/tablas.sql

echo "huella-init: creando usuarios de aplicación y de pruebas."
docker_process_sql <<SQL
CREATE USER '${HUELLA_DB_APP_USER}'@'%' IDENTIFIED BY '${HUELLA_DB_APP_PASSWORD}';
GRANT SELECT, INSERT, UPDATE ON \`sg_huella_carbono_revision\`.* TO '${HUELLA_DB_APP_USER}'@'%';
CREATE USER '${HUELLA_DB_TEST_USER}'@'%' IDENTIFIED BY '${HUELLA_DB_TEST_PASSWORD}';
GRANT ALL PRIVILEGES ON \`sg_huella_carbono_test\`.* TO '${HUELLA_DB_TEST_USER}'@'%';
SQL

echo "huella-init: completado."
