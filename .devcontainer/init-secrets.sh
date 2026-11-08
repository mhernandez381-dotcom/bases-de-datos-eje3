#!/usr/bin/env bash
# Genera .devcontainer/.env con secretos aleatorios SOLO si no existe.
# Nunca sobrescribe un .env existente ni imprime los valores.
set -euo pipefail
cd "$(dirname "$0")"
if [ -f .env ]; then
  chmod 600 .env; setfacl -b .env 2>/dev/null || true
  echo "init-secrets: .devcontainer/.env ya existe; no se modifica su contenido."
  exit 0
fi
gen() { LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c "${1:-24}"; }
umask 077
cat > .env <<ENV
# Generado por init-secrets.sh el $(date -u +%Y-%m-%dT%H:%M:%SZ). NO subir a Git.
# Cambiar valores solo ANTES del primer arranque de MariaDB (volumen vacío).
MARIADB_ROOT_PASSWORD=$(gen 32)
HUELLA_DB_APP_USER=huella_app
HUELLA_DB_APP_PASSWORD=$(gen 32)
HUELLA_DB_TEST_USER=huella_test
HUELLA_DB_TEST_PASSWORD=$(gen 32)
# Contraseña del escritorio noVNC (puerto 6080).
HUELLA_VNC_PASSWORD=$(gen 8)
# Cuenta demo: la semilla la usa solo con el perfil "demo" y solo si la cuenta no existe.
HUELLA_DEMO_NOMBRE=Usuario Demo
HUELLA_DEMO_CORREO=demo@huella.local
HUELLA_DEMO_PASSWORD=$(gen 16)
ENV
# /workspaces tiene ACL por defecto que ignoran umask: forzar permisos.
chmod 600 .env
setfacl -b .env 2>/dev/null || true
echo "init-secrets: creado .devcontainer/.env (permisos 600)."
