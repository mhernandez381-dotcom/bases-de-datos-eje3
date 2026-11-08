package edu.huella.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.huella.dao.UsuarioDAO;
import edu.huella.dto.EmpresaFormulario;
import edu.huella.dto.EmpresaResumen;
import edu.huella.dto.UsuarioSesion;
import edu.huella.exception.AplicacionException;
import edu.huella.exception.ValidacionException;
import edu.huella.session.SesionUsuario;
import edu.huella.soporte.BaseDatosPrueba;
import java.sql.Timestamp;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** A04–A10, A15 contra MariaDB real, con dos propietarios como fixture (no hay gestión multiusuario). */
@SpringBootTest
@ActiveProfiles("integracion")
class EmpresaServiceIT {

    @Autowired EmpresaService servicio;
    @Autowired SesionUsuario sesion;
    @Autowired UsuarioDAO usuarios;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource ds;
    int ana;
    int beto;

    @BeforeEach
    void preparar() {
        BaseDatosPrueba.prepararYVaciar(jdbc, ds);
        ana = usuarios.insertar("Ana", "ana@ejemplo.com", "h");
        beto = usuarios.insertar("Beto", "beto@ejemplo.com", "h");
        sesion.iniciar(new UsuarioSesion(ana, "Ana", "ana@ejemplo.com"));
    }

    @AfterEach
    void cerrar() {
        sesion.cerrar();
    }

    private Map<String, Object> fila(int id) {
        return jdbc.queryForMap("SELECT * FROM empresa WHERE id = ?", id);
    }

    @Test
    void a04CreaConPropietarioDeSesionYTimestamps() {
        int id = servicio.crear(new EmpresaFormulario(" 900-1 ", " ACME "));

        Map<String, Object> f = fila(id);
        assertThat(f.get("id_usuario")).isEqualTo(ana);
        assertThat(f.get("nit")).isEqualTo("900-1");
        assertThat(f.get("nombre")).isEqualTo("ACME");
        assertThat(f.get("creado_en")).isNotNull();
        assertThat(f.get("actualizado_en")).isNotNull();
        assertThat(f.get("eliminado_en")).isNull();
    }

    @Test
    void a05InvalidosNoLleganALaBd() {
        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("", "x"))).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("1".repeat(21), "x"))).isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("1", "x".repeat(151)))).isInstanceOf(ValidacionException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM empresa", Integer.class)).isZero();
    }

    @Test
    void a06NitDuplicadoLoRechazaLaBdConMensajeClaro() {
        servicio.crear(new EmpresaFormulario("900", "Uno"));
        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("900", "Dos")))
                .isInstanceOf(AplicacionException.class)
                .hasMessage("Ya existe una empresa registrada con el NIT 900.");
        // También al editar otra empresa hacia ese NIT.
        int otra = servicio.crear(new EmpresaFormulario("901", "Tres"));
        assertThatThrownBy(() -> servicio.actualizar(otra, new EmpresaFormulario("900", "Tres")))
                .hasMessage("Ya existe una empresa registrada con el NIT 900.");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM empresa WHERE nit = '900'", Integer.class)).isEqualTo(1);
    }

    @Test
    void a07ListarYEditarConservaIdPropietarioYCreacion() throws Exception {
        int id = servicio.crear(new EmpresaFormulario("900", "Original"));
        Map<String, Object> antes = fila(id);
        Thread.sleep(1100); // TIMESTAMP tiene resolución de segundos

        servicio.actualizar(id, new EmpresaFormulario("900-B", "Cambiada"));

        Map<String, Object> despues = fila(id);
        assertThat(despues.get("nit")).isEqualTo("900-B");
        assertThat(despues.get("nombre")).isEqualTo("Cambiada");
        assertThat(despues.get("id_usuario")).isEqualTo(antes.get("id_usuario"));
        assertThat(despues.get("creado_en")).isEqualTo(antes.get("creado_en"));
        assertThat((Timestamp) despues.get("actualizado_en")).isAfter((Timestamp) antes.get("actualizado_en"));
        assertThat(servicio.listar()).extracting(EmpresaResumen::id, EmpresaResumen::nit, EmpresaResumen::nombre)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(id, "900-B", "Cambiada"));
    }

    @Test
    void guardarSinCambiosNoEsUnFalsoError() {
        int id = servicio.crear(new EmpresaFormulario("900", "Igual"));
        servicio.actualizar(id, new EmpresaFormulario("900", "Igual")); // 0 filas cambiadas, 1 encontrada
        assertThat(fila(id).get("nombre")).isEqualTo("Igual");
    }

    @Test
    void a08BorradoLogicoConservaLaFila() {
        int id = servicio.crear(new EmpresaFormulario("900", "Borrable"));
        servicio.eliminar(id);

        assertThat(fila(id).get("eliminado_en")).isNotNull();
        assertThat(servicio.listar()).isEmpty();
    }

    @Test
    void a09NitDeEliminadaNoSeReutilizaNiRestaura() {
        int id = servicio.crear(new EmpresaFormulario("900", "Vieja"));
        servicio.eliminar(id);

        assertThatThrownBy(() -> servicio.crear(new EmpresaFormulario("900", "Nueva")))
                .hasMessageContaining("pertenece a una empresa eliminada");
        assertThat(fila(id).get("eliminado_en")).isNotNull();
        assertThat(fila(id).get("nombre")).isEqualTo("Vieja");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM empresa", Integer.class)).isEqualTo(1);
    }

    @Test
    void a10InexistenteOEliminadaSinFalsoExito() {
        int id = servicio.crear(new EmpresaFormulario("900", "X"));
        servicio.eliminar(id);

        assertThatThrownBy(() -> servicio.eliminar(id)).hasMessage(EmpresaService.MENSAJE_NO_DISPONIBLE);
        assertThatThrownBy(() -> servicio.actualizar(id, new EmpresaFormulario("901", "Y")))
                .hasMessage(EmpresaService.MENSAJE_NO_DISPONIBLE);
        assertThatThrownBy(() -> servicio.eliminar(999_999)).hasMessage(EmpresaService.MENSAJE_NO_DISPONIBLE);
        assertThat(fila(id).get("nit")).isEqualTo("900");
    }

    @Test
    void a15NoSeVenNiModificanEmpresasDeOtroPropietario() {
        jdbc.update("INSERT INTO empresa (id_usuario, nit, nombre) VALUES (?, '800', 'De Beto')", beto);
        int deBeto = jdbc.queryForObject("SELECT id FROM empresa WHERE nit = '800'", Integer.class);
        servicio.crear(new EmpresaFormulario("900", "De Ana"));

        assertThat(servicio.listar()).extracting(EmpresaResumen::nombre).containsExactly("De Ana");
        assertThatThrownBy(() -> servicio.actualizar(deBeto, new EmpresaFormulario("800", "Robada")))
                .hasMessage(EmpresaService.MENSAJE_NO_DISPONIBLE);
        assertThatThrownBy(() -> servicio.eliminar(deBeto)).hasMessage(EmpresaService.MENSAJE_NO_DISPONIBLE);
        Map<String, Object> f = fila(deBeto);
        assertThat(f.get("nombre")).isEqualTo("De Beto");
        assertThat(f.get("id_usuario")).isEqualTo(beto);
        assertThat(f.get("eliminado_en")).isNull();
    }

    @Test
    void a11SinSesionNoHayAccesoAlCrud() {
        sesion.cerrar();
        assertThatThrownBy(servicio::listar).isInstanceOf(AplicacionException.class);
    }
}
