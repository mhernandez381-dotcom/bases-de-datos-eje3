package edu.huella.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Comprueba conexión, zona horaria de sesión y presencia de las 8 tablas. No modifica nada. */
@Component
public class DiagnosticoConexion {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticoConexion.class);
    static final List<String> TABLAS_ESPERADAS = List.of(
            "compensacion", "consumo", "empresa", "factor_emision",
            "fuente_emision", "proyecto", "sede", "usuario");

    private final JdbcTemplate jdbc;

    public DiagnosticoConexion(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String resumen() {
        try {
            String version = jdbc.queryForObject("SELECT VERSION()", String.class);
            String zona = jdbc.queryForObject("SELECT @@session.time_zone", String.class);
            List<String> tablas = jdbc.queryForList(
                    "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() "
                            + "AND TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME", String.class);
            boolean esquemaOk = tablas.equals(TABLAS_ESPERADAS);
            String texto = "MariaDB " + version + "\nZona de sesión: " + zona
                    + "\nTablas: " + tablas.size() + "/8 " + (esquemaOk ? "OK" : "DISCREPANCIA");
            LOG.info("Diagnóstico BD: version={}, zona={}, tablas={}", version, zona, tablas);
            return texto;
        } catch (DataAccessException e) {
            LOG.error("Diagnóstico BD falló: {}", e.getMostSpecificCause().getMessage());
            return "No se pudo conectar con la base de datos.";
        }
    }
}
