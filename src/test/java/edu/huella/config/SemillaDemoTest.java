package edu.huella.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.huella.dao.UsuarioDAO;
import edu.huella.model.Usuario;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SemillaDemoTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UsuarioDAO dao = mock(UsuarioDAO.class);

    private SemillaDemo semilla(String nombre, String correo, String password) {
        return new SemillaDemo(dao, encoder, nombre, correo, password);
    }

    @Test
    void creaLaCuentaConHashBcryptYCorreoNormalizado() {
        when(dao.buscarPorCorreo("demo@huella.local")).thenReturn(Optional.empty());
        when(dao.insertar(anyString(), anyString(), anyString())).thenReturn(1);

        assertThat(semilla(" Usuario Demo ", " Demo@Huella.local", "secreto-123").aplicar())
                .isEqualTo(SemillaDemo.Resultado.CREADA);

        ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
        verify(dao).insertar(eq("Usuario Demo"), eq("demo@huella.local"), hash.capture());
        assertThat(hash.getValue()).startsWith("$2").isNotEqualTo("secreto-123");
        assertThat(encoder.matches("secreto-123", hash.getValue())).isTrue();
    }

    @Test
    void siYaExisteAunqueEsteEliminadaNoLaModifica() {
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(dao.buscarPorCorreo("demo@huella.local"))
                .thenReturn(Optional.of(new Usuario(1, "Demo", "demo@huella.local", "$2a$hash", t, t, t)));

        assertThat(semilla("Demo", "demo@huella.local", "secreto-123").aplicar())
                .isEqualTo(SemillaDemo.Resultado.YA_EXISTIA);
        verify(dao, never()).insertar(anyString(), anyString(), anyString());
    }

    @Test
    void carreraConUniqueSeTrataComoExistente() {
        when(dao.buscarPorCorreo(anyString())).thenReturn(Optional.empty());
        when(dao.insertar(anyString(), anyString(), anyString())).thenThrow(new DuplicateKeyException("correo"));

        assertThat(semilla("Demo", "demo@huella.local", "secreto-123").aplicar())
                .isEqualTo(SemillaDemo.Resultado.YA_EXISTIA);
    }

    @Test
    void bdNoDisponibleNoDetieneLaAplicacion() {
        when(dao.buscarPorCorreo(anyString())).thenThrow(new DataAccessResourceFailureException("caída"));

        assertThat(semilla("Demo", "demo@huella.local", "secreto-123").aplicar())
                .isEqualTo(SemillaDemo.Resultado.BD_NO_DISPONIBLE);
    }

    @Test
    void configuracionIncompletaFallaSinTocarLaBd() {
        assertThatThrownBy(() -> semilla("", "demo@huella.local", "secreto-123").aplicar())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("HUELLA_DEMO_NOMBRE");
        assertThatThrownBy(() -> semilla("Demo", "sin-arroba", "secreto-123").aplicar())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("HUELLA_DEMO_CORREO");
        assertThatThrownBy(() -> semilla("Demo", "demo@huella.local", "corta").aplicar())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("HUELLA_DEMO_PASSWORD")
                .hasMessageNotContaining("corta");
        verify(dao, never()).buscarPorCorreo(anyString());
    }
}
