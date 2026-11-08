#!/usr/bin/env bash
# Restaura un respaldo de scripts/respaldar.sh en una base VACÍA.
# Uso:  scripts/restaurar.sh <archivo.sql.gz> <base_destino> [--crear]
#   --crear  crea la base destino (utf8mb4) si no existe.
# Nunca borra ni sobrescribe: si la base destino tiene tablas, se detiene.
# Usa la cuenta root de MariaDB del .env (la cuenta de la app no tiene DDL).
set -euo pipefail
cd "$(dirname "$0")/.."
ARCHIVO="${1:?uso: scripts/restaurar.sh <archivo.sql.gz> <base_destino> [--crear]}"
BASE="${2:?falta la base destino}"
CREAR="${3:-}"
[ -f "$ARCHIVO" ] || { echo "restaurar: no existe $ARCHIVO" >&2; exit 1; }
[[ "$BASE" =~ ^[A-Za-z0-9_]+$ ]] || { echo "restaurar: nombre de base no válido" >&2; exit 1; }

umask 077
CREDENCIALES=$(mktemp)
trap 'rm -f "$CREDENCIALES"' EXIT
printf '[client]\nuser=root\npassword=%s\nhost=%s\nport=%s\n' \
  "$(grep -E '^MARIADB_ROOT_PASSWORD=' .devcontainer/.env | cut -d= -f2-)" \
  "${HUELLA_DB_HOST:-db}" "${HUELLA_DB_PORT:-3306}" > "$CREDENCIALES"
sql() { mariadb --defaults-extra-file="$CREDENCIALES" -N -e "$1"; }

EXISTE=$(sql "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$BASE'")
if [ "$EXISTE" = "0" ]; then
  if [ "$CREAR" != "--crear" ]; then
    echo "restaurar: la base $BASE no existe (usa --crear)" >&2; exit 1
  fi
  sql "CREATE DATABASE \`$BASE\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci"
  echo "restaurar: base $BASE creada."
fi
TABLAS=$(sql "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='$BASE'")
if [ "$TABLAS" != "0" ]; then
  echo "restaurar: $BASE tiene $TABLAS tablas; solo se restaura en una base vacía. No se hizo nada." >&2
  exit 1
fi
gunzip -c "$ARCHIVO" | mariadb --defaults-extra-file="$CREDENCIALES" "$BASE"
echo "restaurar: completado en $BASE ($(sql "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='$BASE'") tablas)."
