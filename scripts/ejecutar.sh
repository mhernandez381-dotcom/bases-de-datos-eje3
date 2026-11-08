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
  set -a; . .devcontainer/.env; set +a   # HUELLA_DEMO_* solo para este proceso
  PERFIL=(--spring.profiles.active=demo)
fi
export DISPLAY="${DISPLAY:-:1}"
exec java -jar "$JAR" "${PERFIL[@]}"
