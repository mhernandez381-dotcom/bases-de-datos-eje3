package edu.huella.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.huella.model.Empresa;
import edu.huella.soporte.BaseDatosPrueba;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** EmpresaDaoJdbc contra MariaDB real: UNIQUE, FK, filtros y filas afectadas. */
@SpringBootTest
@ActiveProfiles("integracion")
class EmpresaDaoJdbcIT {

    @Autowired EmpresaDAO dao;
    @Autowired UsuarioDAO usuarios;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource ds;
    int ana;

    @BeforeEach
    void preparar() {
        BaseDatosPrueba.prepararYVaciar(jdbc, ds);
        ana = usuarios.insertar("Ana", "ana@ejemplo.com", "h");
    }

    @Test
    void insertaYListaActivasOrdenadas() {
        dao.insertar(ana, "2", "Beta");
        dao.insertar(ana, "1", "Alfa");
        assertThat(dao.listarActivasPorUsuario(ana)).extracting(Empresa::nombre).containsExactly("Alfa", "Beta");
    }

    @Test
    void uniqueDeNitYFkDeUsuario() {
        dao.insertar(ana, "900", "A");
        assertThatThrownBy(() -> dao.insertar(ana, "900", "B")).isInstanceOf(DuplicateKeyException.class);
        assertThatThrownBy(() -> dao.insertar(999_999, "901", "Sin dueño"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void filasAfectadasYBorradoLogico() {
        int id = dao.insertar(ana, "900", "A");
        assertThat(dao.actualizar(id, ana, "900", "A")).isEqualTo(1); // filas encontradas
        assertThat(dao.marcarEliminada(id, ana)).isEqualTo(1);
        assertThat(dao.marcarEliminada(id, ana)).isZero();
        assertThat(dao.actualizar(id, ana, "900", "B")).isZero();
        assertThat(dao.listarActivasPorUsuario(ana)).isEmpty();
        assertThat(dao.buscarPorNit("900")).get().satisfies(e -> assertThat(e.activa()).isFalse());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM empresa", Integer.class)).isEqualTo(1);
    }
}
