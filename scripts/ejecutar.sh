#!/usr/bin/env bash
# Compila (si hace falta) y abre la aplicación en el escritorio remoto (DISPLAY :1).
# Uso, desde la terminal del Codespace:  scripts/ejecutar.sh [--demo]
#   --demo  activa el perfil "demo" (crea la cuenta demo SOLO si no existe).
set -euo pipefail
cd "$(dirname "$0")/.."
JAR=target/huella-empresas-0.1.0.jar
if [ ! -f "$JAR" ] || [ -n "$(find src pom.xml -newer "$JAR" -print -quit)" ]; then
  ./mvnw -B -q package -DskipTests
fi
PERFIL=()
if [ "${1:-}" = "--demo" ]; then
  # Solo HUELLA_DEMO_* y solo para este proceso. No se hace `source` del .env:
  # sus valores no van entre comillas (formato de docker compose) y alguno lleva espacios.
  while IFS='=' read -r clave valor; do
    export "$clave=$valor"
  done < <(grep -E '^HUELLA_DEMO_(NOMBRE|CORREO|PASSWORD)=' .devcontainer/.env)
  PERFIL=(--spring.profiles.active=demo)
fi
export DISPLAY="${DISPLAY:-:1}"
exec java -jar "$JAR" "${PERFIL[@]}"
