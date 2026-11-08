package edu.huella.model;

import java.time.LocalDateTime;

/**
 * Fila de la tabla {@code usuario}. Las fechas son {@link LocalDateTime} en UTC
 * (cada conexión fuerza {@code time_zone = '+00:00'}). {@code eliminadoEn == null} = activo.
 * Contiene el hash: no debe llegar a la vista; para la sesión se usa {@code UsuarioSesion}.
 */
public record Usuario(
        Integer id,
        String nombre,
        String correo,
        String passwordHash,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,
        LocalDateTime eliminadoEn) {

    public boolean activo() {
        return eliminadoEn == null;
    }

    /** Evita que el hash aparezca en logs o depuración. */
    @Override
    public String toString() {
        return "Usuario[id=" + id + ", correo=" + correo + ", activo=" + activo() + "]";
    }
}
