package edu.huella.dto;

import java.time.LocalDateTime;

/** Fila de la tabla de empresas en la vista. Fechas en UTC. */
public record EmpresaResumen(int id, String nit, String nombre,
                             LocalDateTime creadoEn, LocalDateTime actualizadoEn) {
}
