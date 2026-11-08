package edu.huella.dao;

import edu.huella.model.Usuario;
import java.util.Optional;

/** Acceso a la tabla {@code usuario}. El correo se recibe ya normalizado. */
public interface UsuarioDAO {

    /** Busca por correo incluyendo usuarios eliminados lógicamente (lo usa la semilla). */
    Optional<Usuario> buscarPorCorreo(String correo);

    /** Busca solo usuarios activos ({@code eliminado_en IS NULL}); lo usa el login. */
    Optional<Usuario> buscarActivoPorCorreo(String correo);

    /**
     * Inserta un usuario y devuelve su ID generado.
     * Lanza {@code DuplicateKeyException} si el correo ya existe (restricción UNIQUE).
     */
    int insertar(String nombre, String correo, String passwordHash);
}
