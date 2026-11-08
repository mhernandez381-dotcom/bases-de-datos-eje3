#!/usr/bin/env bash
# Deriva db/esquema/tablas.sql desde la SECCIÓN 1 del SQL de referencia.
# - Conserva literalmente tablas, índices, AUTO_INCREMENT, FK y CHECK (líneas 35-335).
# - Excluye CREATE DATABASE / USE (líneas 40-41): la base la crea el inicializador.
# - Excluye la SECCIÓN 2 (datos) y la SECCIÓN 3 (respaldo): nunca se ejecutan al arrancar.
# Uso: db/derivar-esquema.sh            -> regenera el archivo
#      db/derivar-esquema.sh --verificar -> falla si el archivo versionado difiere
set -euo pipefail
cd "$(dirname "$0")/.."
REF=referencias/sg_huella_carbono_entrega.sql
SHA_ESPERADO=cd35e68b7139d8c8bf1145807cbbe0c32138f19a41b0425d8ef17c9a82c777a3
SHA_REAL=$(sha256sum "$REF" | cut -d' ' -f1)
if [ "$SHA_REAL" != "$SHA_ESPERADO" ]; then
  echo "ERROR: el SQL de referencia cambió (sha256 $SHA_REAL). Revisar antes de derivar." >&2
  exit 1
fi
generar() {
  echo "-- ARCHIVO GENERADO por db/derivar-esquema.sh. No editar a mano."
  echo "-- Origen: $REF (sha256 $SHA_ESPERADO), SECCIÓN 1, líneas 35-39 y 42-335."
  echo "-- Sin CREATE DATABASE/USE, sin datos y sin bloque de respaldo."
  echo "-- Se aplica una sola vez sobre una base VACÍA ya seleccionada."
  sed -n '35,39p' "$REF"
  sed -n '42,335p' "$REF"
}
if [ "${1:-}" = "--verificar" ]; then
  diff <(generar) db/esquema/tablas.sql && echo "OK: db/esquema/tablas.sql coincide con la referencia."
else
  generar > db/esquema/tablas.sql
  echo "Generado db/esquema/tablas.sql"
fi
