package edu.huella.config;

import static org.assertj.core.api.Assertions.assertThat;

import edu.huella.dao.UsuarioDAO;
import edu.huella.soporte.BaseDatosPrueba;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/** A02: la semilla ejecutada dos veces deja una sola cuenta y conserva el hash. */
@SpringBootTest
@ActiveProfiles("integracion")
class SemillaDemoIT {

    @Autowired UsuarioDAO dao;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource ds;

    @BeforeEach
    void limpiar() {
        BaseDatosPrueba.prepararYVaciar(jdbc, ds);
    }

    private SemillaDemo semilla(String password) {
        return new SemillaDemo(dao, encoder, "Usuario Demo", "Demo@Huella.local", password);
    }

    private String hashActual() {
        return jdbc.queryForObject("SELECT password_hash FROM usuario WHERE correo = 'demo@huella.local'", String.class);
    }

    @Test
    void dosEjecucionesNoDuplicanNiCambianElHash() {
        assertThat(semilla("secreto-123").aplicar()).isEqualTo(SemillaDemo.Resultado.CREADA);
        String hashInicial = hashActual();
        assertThat(hashInicial).startsWith("$2").doesNotContain("secreto-123");
        assertThat(encoder.matches("secreto-123", hashInicial)).isTrue();

        // Segunda ejecución, incluso con otra contraseña configurada: no resetea.
        assertThat(semilla("otra-clave-456").aplicar()).isEqualTo(SemillaDemo.Resultado.YA_EXISTIA);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class)).isEqualTo(1);
        assertThat(hashActual()).isEqualTo(hashInicial);
    }

    @Test
    void noReactivaUnaCuentaEliminada() {
        semilla("secreto-123").aplicar();
        jdbc.update("UPDATE usuario SET eliminado_en = CURRENT_TIMESTAMP");

        assertThat(semilla("secreto-123").aplicar()).isEqualTo(SemillaDemo.Resultado.YA_EXISTIA);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE eliminado_en IS NOT NULL", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class)).isEqualTo(1);
    }
}
