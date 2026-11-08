package edu.huella.model;

import java.time.LocalDateTime;

/** Fila de la tabla {@code empresa}. Fechas en UTC; {@code eliminadoEn == null} = activa. */
public record Empresa(
        Integer id,
        int idUsuario,
        String nit,
        String nombre,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,
        LocalDateTime eliminadoEn) {

    public boolean activa() {
        return eliminadoEn == null;
    }
}
