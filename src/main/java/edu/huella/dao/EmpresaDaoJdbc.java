package edu.huella.dao;

import edu.huella.model.Empresa;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Implementación JDBC con consultas parametrizadas.
 * Nota: Connector/J devuelve por defecto filas ENCONTRADAS en UPDATE (useAffectedRows=false),
 * así que guardar sin cambios devuelve 1, no 0.
 */
@Repository
public class EmpresaDaoJdbc implements EmpresaDAO {

    private static final String COLUMNAS =
            "id, id_usuario, nit, nombre, creado_en, actualizado_en, eliminado_en";

    private final JdbcTemplate jdbc;

    public EmpresaDaoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Empresa> listarActivasPorUsuario(int idUsuario) {
        return jdbc.query("SELECT " + COLUMNAS + " FROM empresa "
                        + "WHERE id_usuario = ? AND eliminado_en IS NULL ORDER BY nombre, id",
                EmpresaDaoJdbc::mapear, idUsuario);
    }

    @Override
    public Optional<Empresa> buscarPorNit(String nit) {
        return jdbc.query("SELECT " + COLUMNAS + " FROM empresa WHERE nit = ?",
                EmpresaDaoJdbc::mapear, nit).stream().findFirst();
    }

    @Override
    public int insertar(int idUsuario, String nit, String nombre) {
        KeyHolder claves = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO empresa (id_usuario, nit, nombre) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idUsuario);
            ps.setString(2, nit);
            ps.setString(3, nombre);
            return ps;
        }, claves);
        return Objects.requireNonNull(claves.getKey(), "MariaDB no devolvió el ID generado").intValue();
    }

    @Override
    public int actualizar(int id, int idUsuario, String nit, String nombre) {
        return jdbc.update("UPDATE empresa SET nit = ?, nombre = ? "
                        + "WHERE id = ? AND id_usuario = ? AND eliminado_en IS NULL",
                nit, nombre, id, idUsuario);
    }

    @Override
    public int marcarEliminada(int id, int idUsuario) {
        return jdbc.update("UPDATE empresa SET eliminado_en = CURRENT_TIMESTAMP "
                        + "WHERE id = ? AND id_usuario = ? AND eliminado_en IS NULL",
                id, idUsuario);
    }

    private static Empresa mapear(ResultSet rs, int fila) throws SQLException {
        return new Empresa(
                rs.getInt("id"),
                rs.getInt("id_usuario"),
                rs.getString("nit"),
                rs.getString("nombre"),
                rs.getObject("creado_en", LocalDateTime.class),
                rs.getObject("actualizado_en", LocalDateTime.class),
                rs.getObject("eliminado_en", LocalDateTime.class));
    }
}
