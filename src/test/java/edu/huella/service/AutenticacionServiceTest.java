package edu.huella.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.huella.dao.UsuarioDAO;
import edu.huella.dto.UsuarioSesion;
import edu.huella.exception.AplicacionException;
import edu.huella.exception.CredencialesInvalidasException;
import edu.huella.model.Usuario;
import edu.huella.session.SesionUsuario;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AutenticacionServiceTest {

    // Coste 4: rápido en pruebas unitarias; la aplicación usa el coste por defecto (10).
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final String hash = encoder.encode("clave-correcta");
    private UsuarioDAO dao;
    private SesionUsuario sesion;
    private AutenticacionService servicio;

    @BeforeEach
    void preparar() {
        dao = mock(UsuarioDAO.class);
        sesion = new SesionUsuario();
        servicio = new AutenticacionService(dao, encoder, sesion);
    }

    private Usuario usuario(String hashGuardado) {
        LocalDateTime t = LocalDateTime.of(2026, 11, 8, 12, 0);
        return new Usuario(7, "Ana", "ana@ejemplo.com", hashGuardado, t, t, null);
    }

    @Test
    void loginCorrectoIniciaSesionSinExponerHash() {
        when(dao.buscarActivoPorCorreo("ana@ejemplo.com")).thenReturn(Optional.of(usuario(hash)));

        UsuarioSesion s = servicio.iniciarSesion("  Ana@Ejemplo.COM ", "clave-correcta".toCharArray());

        assertThat(s).isEqualTo(new UsuarioSesion(7, "Ana", "ana@ejemplo.com"));
        assertThat(sesion.actual()).contains(s);
        assertThat(UsuarioSesion.class.getRecordComponents())
                .extracting(c -> c.getName()).containsExactly("id", "nombre", "correo");
    }

    @Test
    void passwordIncorrectaDaMensajeGenericoYNoIniciaSesion() {
        when(dao.buscarActivoPorCorreo("ana@ejemplo.com")).thenReturn(Optional.of(usuario(hash)));

        assertThatThrownBy(() -> servicio.iniciarSesion("ana@ejemplo.com", "otra".toCharArray()))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage(CredencialesInvalidasException.MENSAJE);
        assertThat(sesion.actual()).isEmpty();
    }

    @Test
    void correoInexistenteOEliminadoDaElMismoMensaje() {
        // El DAO de activos no devuelve usuarios eliminados.
        when(dao.buscarActivoPorCorreo(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.iniciarSesion("nadie@ejemplo.com", "clave-correcta".toCharArray()))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage(CredencialesInvalidasException.MENSAJE);
        assertThat(sesion.actual()).isEmpty();
    }

    @Test
    void camposVaciosSeRechazanSinConsultarLaBd() {
        assertThatThrownBy(() -> servicio.iniciarSesion("   ", "x".toCharArray()))
                .isInstanceOf(AplicacionException.class).hasMessageContaining("correo y la contraseña");
        assertThatThrownBy(() -> servicio.iniciarSesion("ana@ejemplo.com", new char[0]))
                .isInstanceOf(AplicacionException.class);
        verify(dao, never()).buscarActivoPorCorreo(anyString());
    }

    @Test
    void passwordDeMasDe72BytesSeRechaza() {
        char[] larga = new char[73];
        Arrays.fill(larga, 'a');
        assertThatThrownBy(() -> servicio.iniciarSesion("ana@ejemplo.com", larga))
                .isInstanceOf(CredencialesInvalidasException.class);
        verify(dao, never()).buscarActivoPorCorreo(anyString());
    }

    @Test
    void fallaDeBdDaMensajeSeguro() {
        when(dao.buscarActivoPorCorreo(anyString()))
                .thenThrow(new DataAccessResourceFailureException("Connection refused jdbc:mariadb://db:3306 user=x"));

        assertThatThrownBy(() -> servicio.iniciarSesion("ana@ejemplo.com", "clave".toCharArray()))
                .isInstanceOf(AplicacionException.class)
                .isNotInstanceOf(CredencialesInvalidasException.class)
                .hasMessage(AutenticacionService.MENSAJE_BD_NO_DISPONIBLE);
        assertThat(sesion.actual()).isEmpty();
    }

    @Test
    void cerrarSesionLaLimpia() {
        when(dao.buscarActivoPorCorreo("ana@ejemplo.com")).thenReturn(Optional.of(usuario(hash)));
        servicio.iniciarSesion("ana@ejemplo.com", "clave-correcta".toCharArray());

        servicio.cerrarSesion();

        assertThat(sesion.actual()).isEmpty();
        assertThatThrownBy(sesion::requerir).isInstanceOf(AplicacionException.class);
    }
}
