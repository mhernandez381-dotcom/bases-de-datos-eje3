# Imagen de desarrollo: JDK 21 (Microsoft Build of OpenJDK) sobre Debian bookworm.
FROM mcr.microsoft.com/devcontainers/java:3.1.7-21-bookworm

# Cliente MariaDB (scripts de verificación/respaldo) y utilidades X11 para
# capturar/inspeccionar el escritorio remoto durante la validación visual.
RUN apt-get update \
 && export DEBIAN_FRONTEND=noninteractive \
 && apt-get install -y --no-install-recommends mariadb-client imagemagick x11-utils xdotool \
 && apt-get clean && rm -rf /var/lib/apt/lists/*
