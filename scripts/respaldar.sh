#!/usr/bin/env bash
# Exporta una base (por defecto la operativa) a respaldos/<base>-<fecha UTC>.sql.gz
# Uso:  scripts/respaldar.sh [base]
# - Usa el usuario de la aplicación (solo lectura necesaria) y --single-transaction:
#   copia consistente sin bloquear tablas.
# - Sin CREATE DATABASE/USE ni DROP TABLE: se puede restaurar en cualquier base VACÍA.
# - El archivo contiene datos y hashes de contraseña: queda con permisos 600 y
#   respaldos/ está en .gitignore. Cópialo FUERA del Codespace antes de eliminarlo.
set -euo pipefail
cd "$(dirname "$0")/.."
BASE="${1:-sg_huella_carbono_revision}"
[[ "$BASE" =~ ^[A-Za-z0-9_]+$ ]] || { echo "respaldar: nombre de base no válido" >&2; exit 1; }

USUARIO="${HUELLA_DB_USER:-$(grep -E '^HUELLA_DB_APP_USER=' .devcontainer/.env | cut -d= -f2-)}"
CLAVE="${HUELLA_DB_PASSWORD:-$(grep -E '^HUELLA_DB_APP_PASSWORD=' .devcontainer/.env | cut -d= -f2-)}"
if [ "$BASE" = "sg_huella_carbono_test" ]; then
  USUARIO="${HUELLA_TEST_DB_USER:?}"; CLAVE="${HUELLA_TEST_DB_PASSWORD:?}"
fi

umask 077
CREDENCIALES=$(mktemp)
trap 'rm -f "$CREDENCIALES"' EXIT
printf '[client]\nuser=%s\npassword=%s\nhost=%s\nport=%s\n' \
  "$USUARIO" "$CLAVE" "${HUELLA_DB_HOST:-db}" "${HUELLA_DB_PORT:-3306}" > "$CREDENCIALES"

mkdir -p respaldos
DESTINO="respaldos/${BASE}-$(date -u +%Y%m%dT%H%M%SZ).sql.gz"
mariadb-dump --defaults-extra-file="$CREDENCIALES" --single-transaction --no-tablespaces \
  --skip-add-drop-table --triggers --default-character-set=utf8mb4 \
  --tz-utc "$BASE" | gzip -9 > "$DESTINO"
chmod 600 "$DESTINO"
echo "respaldar: $DESTINO ($(du -h "$DESTINO" | cut -f1))"
