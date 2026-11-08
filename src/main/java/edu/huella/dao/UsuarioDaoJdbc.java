package edu.huella.dao;

import edu.huella.model.Usuario;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Implementación JDBC con consultas parametrizadas. */
@Repository
public class UsuarioDaoJdbc implements UsuarioDAO {

    private static final String COLUMNAS =
            "id, nombre, correo, password_hash, creado_en, actualizado_en, eliminado_en";

    private final JdbcTemplate jdbc;

    public UsuarioDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return jdbc.query("SELECT " + COLUMNAS + " FROM usuario WHERE correo = ?",
                UsuarioDaoJdbc::mapear, correo).stream().findFirst();
    }

    @Override
    public Optional<Usuario> buscarActivoPorCorreo(String correo) {
        return jdbc.query("SELECT " + COLUMNAS + " FROM usuario WHERE correo = ? AND eliminado_en IS NULL",
                UsuarioDaoJdbc::mapear, correo).stream().findFirst();
    }

    @Override
    public int insertar(String nombre, String correo, String passwordHash) {
        KeyHolder claves = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuario (nombre, correo, password_hash) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, passwordHash);
            return ps;
        }, claves);
        return Objects.requireNonNull(claves.getKey(), "MariaDB no devolvió el ID generado").intValue();
    }

    private static Usuario mapear(ResultSet rs, int fila) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("password_hash"),
                rs.getObject("creado_en", LocalDateTime.class),
                rs.getObject("actualizado_en", LocalDateTime.class),
                rs.getObject("eliminado_en", LocalDateTime.class));
    }
}
