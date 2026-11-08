package edu.huella.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.huella.dao.UsuarioDAO;
import edu.huella.dto.UsuarioSesion;
import edu.huella.exception.CredencialesInvalidasException;
import edu.huella.session.SesionUsuario;
import edu.huella.soporte.BaseDatosPrueba;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/** A03 y A11: login correcto, incorrecto y de usuario eliminado contra MariaDB real; logout. */
@SpringBootTest
@ActiveProfiles("integracion")
class AutenticacionServiceIT {

    @Autowired AutenticacionService servicio;
    @Autowired SesionUsuario sesion;
    @Autowired UsuarioDAO dao;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource ds;
    int idAna;

    @BeforeEach
    void preparar() {
        sesion.cerrar();
        BaseDatosPrueba.prepararYVaciar(jdbc, ds);
        idAna = dao.insertar("Ana", "ana@ejemplo.com", encoder.encode("clave-correcta"));
        dao.insertar("Beto", "beto@ejemplo.com", encoder.encode("clave-beto"));
        jdbc.update("UPDATE usuario SET eliminado_en = CURRENT_TIMESTAMP WHERE correo = 'beto@ejemplo.com'");
    }

    @Test
    void loginCorrectoYLogout() {
        UsuarioSesion s = servicio.iniciarSesion("ANA@ejemplo.com ", "clave-correcta".toCharArray());
        assertThat(s).isEqualTo(new UsuarioSesion(idAna, "Ana", "ana@ejemplo.com"));
        assertThat(sesion.requerir()).isEqualTo(s);

        servicio.cerrarSesion();
        assertThat(sesion.actual()).isEmpty();
    }

    @Test
    void passwordIncorrecta() {
        assertThatThrownBy(() -> servicio.iniciarSesion("ana@ejemplo.com", "mala".toCharArray()))
                .isInstanceOf(CredencialesInvalidasException.class);
        assertThat(sesion.actual()).isEmpty();
    }

    @Test
    void usuarioEliminadoNoEntraAunqueLaClaveSeaCorrecta() {
        assertThatThrownBy(() -> servicio.iniciarSesion("beto@ejemplo.com", "clave-beto".toCharArray()))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage(CredencialesInvalidasException.MENSAJE);
        assertThat(sesion.actual()).isEmpty();
    }
}
