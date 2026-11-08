package edu.huella.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.huella.model.Usuario;
import edu.huella.soporte.BaseDatosPrueba;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** UsuarioDaoJdbc contra MariaDB real (base sg_huella_carbono_test). */
@SpringBootTest
@ActiveProfiles("integracion")
class UsuarioDaoJdbcIT {

    @Autowired UsuarioDAO dao;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource ds;

    @BeforeEach
    void limpiar() {
        BaseDatosPrueba.prepararYVaciar(jdbc, ds);
    }

    @Test
    void sesionEnUtcYMariaDbReal() {
        assertThat(jdbc.queryForObject("SELECT @@session.time_zone", String.class)).isEqualTo("+00:00");
        assertThat(jdbc.queryForObject("SELECT VERSION()", String.class)).contains("MariaDB");
    }

    @Test
    void insertaYBuscaConTimestampsDeLaBd() {
        int id = dao.insertar("Ana", "ana@ejemplo.com", "$2a$10$hashdeprueba");

        Usuario u = dao.buscarPorCorreo("ana@ejemplo.com").orElseThrow();
        assertThat(u.id()).isEqualTo(id);
        assertThat(u.nombre()).isEqualTo("Ana");
        assertThat(u.passwordHash()).isEqualTo("$2a$10$hashdeprueba");
        assertThat(u.eliminadoEn()).isNull();
        assertThat(u.creadoEn()).isNotNull();
        assertThat(u.actualizadoEn()).isNotNull();
        // Los LocalDateTime representan UTC.
        assertThat(Duration.between(u.creadoEn(), LocalDateTime.now(ZoneOffset.UTC)).abs())
                .isLessThan(Duration.ofMinutes(2));
        assertThat(dao.buscarActivoPorCorreo("ana@ejemplo.com")).isPresent();
    }

    @Test
    void correoUnicoLoGarantizaLaBd() {
        dao.insertar("Ana", "ana@ejemplo.com", "h1");
        assertThatThrownBy(() -> dao.insertar("Otra", "ana@ejemplo.com", "h2"))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void eliminadoNoApareceEnActivosPeroSiEnBusquedaGeneral() {
        dao.insertar("Ana", "ana@ejemplo.com", "h1");
        jdbc.update("UPDATE usuario SET eliminado_en = CURRENT_TIMESTAMP WHERE correo = ?", "ana@ejemplo.com");

        assertThat(dao.buscarActivoPorCorreo("ana@ejemplo.com")).isEmpty();
        assertThat(dao.buscarPorCorreo("ana@ejemplo.com")).get()
                .satisfies(u -> assertThat(u.eliminadoEn()).isNotNull());
    }

    @Test
    void inexistenteDevuelveVacio() {
        assertThat(dao.buscarPorCorreo("nadie@ejemplo.com")).isEmpty();
    }
}
