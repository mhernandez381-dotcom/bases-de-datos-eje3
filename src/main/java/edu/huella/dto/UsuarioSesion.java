package edu.huella.dto;

/** Datos mínimos del usuario autenticado. Nunca incluye contraseña ni hash. */
public record UsuarioSesion(int id, String nombre, String correo) {
}
