#!/usr/bin/env bash
# Aplica la contraseña VNC del .env (HUELLA_VNC_PASSWORD) en cada arranque.
# Motivo: desktop-lite fija una contraseña al CONSTRUIR la imagen, y tigervncserver
# copia el archivo de contraseña al iniciar; por eso hay que reescribirlo y
# reiniciar Xtigervnc (el bucle de desktop-lite lo relanza en ~5 s).
# Verificado con una autenticación RFB real (ver ESTADO.md).
set -euo pipefail
ENV_FILE="$(dirname "$0")/.env"
PASSWD_FILE=/usr/local/etc/vscode-dev-containers/vnc-passwd
VNC_PW=$(grep -E '^HUELLA_VNC_PASSWORD=' "$ENV_FILE" | cut -d= -f2- || true)
if [ -z "$VNC_PW" ]; then
  echo "post-start: falta HUELLA_VNC_PASSWORD en .devcontainer/.env" >&2
  exit 1
fi
printf '%s\n' "$VNC_PW" | vncpasswd -f | sudo tee "$PASSWD_FILE" > /dev/null
sudo chown "$(id -u):$(id -g)" "$PASSWD_FILE"
chmod 600 "$PASSWD_FILE"

# Reiniciar el servidor VNC para que tome la contraseña nueva.
pkill -u "$(id -u)" -x Xtigervnc || true
for _ in $(seq 1 30); do
  sleep 1
  if pgrep -u "$(id -u)" -x Xtigervnc > /dev/null; then
    echo "post-start: contraseña VNC aplicada y servidor VNC reiniciado."
    exit 0
  fi
done
echo "post-start: Xtigervnc no volvió a arrancar; revisar /tmp/Xtigervnc.log" >&2
exit 1
