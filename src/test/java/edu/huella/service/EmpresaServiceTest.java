package edu.huella.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import edu.huella.dao.EmpresaDAO;
import edu.huella.dto.EmpresaFormulario;
import edu.huella.dto.UsuarioSesion;
import edu.huella.exception.AplicacionException;
import edu.huella.exception.ValidacionException;
import edu.huella.model.Empresa;
import edu.huella.session.SesionUsuario;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;

class EmpresaServiceTest {

    private final EmpresaDAO dao = mock(EmpresaDAO.class);
    private final SesionUsuario sesion = new SesionUsuario();
    private final EmpresaService servicio = new EmpresaService(dao, sesion);
    private static final LocalDateTime T = LocalDateTime.of(2026, 11, 8, 12, 0);

    @BeforeEach
    void iniciarSesion() {
        sesion.iniciar(new UsuarioSesion(5, "Ana", "ana@ejemplo.com"));
    }

    @Test
    void creaConPropietarioDeSesionYDatosRecortados() {
        when(dao.insertar(5, "900123", "ACME S.A.")).thenReturn(42);

        assertThat(servicio.crear(new EmpresaFormulario("  900123 ", "\tACME S.A.  "))).isEqualTo(42);
        verify(dao).insertar(5, "900123", "ACME S.A.");
    }

    @Test
    void vaciosYExcesivosSeRechazanAntesDeLaBd() {
        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("   ", "")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(e -> assertThat(((ValidacionException) e).errores())
                        .containsExactly("El NIT es obligatorio.", "El nombre es obligatorio."));
        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("1".repeat(21), "x".repeat(151))))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("máximo 20").hasMessageContaining("máximo 150");
        assertThatThrownBy(() -> servicio.actualizar(1, new EmpresaFormulario(null, null)))
                .isInstanceOf(ValidacionException.class);
        verifyNoInteractions(dao);
    }

    @Test
    void limitesExactosSeAceptanContandoCaracteres() {
        // 20 caracteres con tildes (más de 20 bytes) siguen siendo válidos para VARCHAR(20).
        String nit = "ñ".repeat(20);
        String nombre = "é".repeat(150);
        assertThat(EmpresaService.validar(new EmpresaFormulario(nit, nombre)))
                .isEqualTo(new EmpresaFormulario(nit, nombre));
    }

    @Test
    void nitDuplicadoActivoSeExplica() {
        when(dao.insertar(anyInt(), anyString(), anyString())).thenThrow(new DuplicateKeyException("nit"));
        when(dao.buscarPorNit("900")).thenReturn(Optional.of(new Empresa(1, 9, "900", "Otra", T, T, null)));

        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("900", "Nueva")))
                .isInstanceOf(AplicacionException.class)
                .hasMessage("Ya existe una empresa registrada con el NIT 900.");
    }

    @Test
    void nitDeEmpresaEliminadaSeExplicaSinRestaurar() {
        when(dao.actualizar(anyInt(), anyInt(), anyString(), anyString())).thenThrow(new DuplicateKeyException("nit"));
        when(dao.buscarPorNit("900")).thenReturn(Optional.of(new Empresa(1, 5, "900", "Vieja", T, T, T)));

        assertThatThrownBy(() -> servicio.actualizar(2, new EmpresaFormulario("900", "Nueva")))
                .hasMessageContaining("pertenece a una empresa eliminada")
                .hasMessageContaining("no se restaura");
    }

    @Test
    void actualizarOEliminarSinFilasNoFingeExito() {
        when(dao.actualizar(99, 5, "1", "x")).thenReturn(0);
        when(dao.marcarEliminada(99, 5)).thenReturn(0);

        assertThatThrownBy(() -> servicio.actualizar(99, new EmpresaFormulario("1", "x")))
                .hasMessage(EmpresaService.MENSAJE_NO_DISPONIBLE);
        assertThatThrownBy(() -> servicio.eliminar(99))
                .hasMessage(EmpresaService.MENSAJE_NO_DISPONIBLE);
    }

    @Test
    void sinSesionNoSeAccedeALaBd() {
        sesion.cerrar();
        assertThatThrownBy(servicio::listar).hasMessageContaining("sesión no está iniciada");
        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("1", "x"))).isInstanceOf(AplicacionException.class);
        assertThatThrownBy(() -> servicio.eliminar(1)).isInstanceOf(AplicacionException.class);
        verifyNoInteractions(dao);
    }

    @Test
    void fallaDeConexionDaMensajeSeguro() {
        when(dao.listarActivasPorUsuario(5)).thenThrow(new DataAccessResourceFailureException("jdbc:mariadb://db secreto"));
        assertThatThrownBy(servicio::listar)
                .hasMessage(AutenticacionService.MENSAJE_BD_NO_DISPONIBLE)
                .hasMessageNotContaining("jdbc");
    }
}
