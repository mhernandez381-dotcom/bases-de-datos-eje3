package edu.huella.soporte;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Prepara la base de PRUEBAS. Se niega a actuar sobre otra base.
 * Si está vacía aplica db/esquema/tablas.sql; si tiene las 8 tablas, la reutiliza;
 * cualquier otro estado (esquema parcial) es un error: no hace DROP.
 */
public final class BaseDatosPrueba {

    public static final String NOMBRE = "sg_huella_carbono_test";
    private static final List<String> TABLAS = List.of(
            "compensacion", "consumo", "empresa", "factor_emision",
            "fuente_emision", "proyecto", "sede", "usuario");
    /** Orden hijo → padre para vaciar respetando las FK. */
    private static final List<String> ORDEN_BORRADO = List.of(
            "compensacion", "consumo", "sede", "factor_emision", "fuente_emision",
            "proyecto", "empresa", "usuario");

    private BaseDatosPrueba() {
    }

    public static synchronized void prepararYVaciar(JdbcTemplate jdbc, DataSource ds) {
        String actual = jdbc.queryForObject("SELECT DATABASE()", String.class);
        if (!NOMBRE.equals(actual)) {
            throw new IllegalStateException("Las pruebas solo pueden usar " + NOMBRE + ", no " + actual);
        }
        List<String> tablas = jdbc.queryForList(
                "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME", String.class);
        if (tablas.isEmpty()) {
            new ResourceDatabasePopulator(new FileSystemResource(Path.of("db/esquema/tablas.sql")))
                    .execute(ds);
        } else if (!tablas.equals(TABLAS)) {
            throw new IllegalStateException("Esquema de pruebas inesperado: " + tablas);
        }
        jdbc.execute((ConnectionCallback<Void>) con -> {
            try (Statement st = con.createStatement()) {
                for (String t : ORDEN_BORRADO) {
                    st.executeUpdate("DELETE FROM `" + t + "`"); // nombres fijos, sin entrada externa
                }
            }
            return null;
        });
    }
}
