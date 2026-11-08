package edu.huella.service;

import edu.huella.dao.EmpresaDAO;
import edu.huella.dto.EmpresaFormulario;
import edu.huella.dto.EmpresaResumen;
import edu.huella.dto.UsuarioSesion;
import edu.huella.exception.AplicacionException;
import edu.huella.exception.ValidacionException;
import edu.huella.model.Empresa;
import edu.huella.session.SesionUsuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * Reglas del CRUD de empresas. El propietario SIEMPRE sale de la sesión.
 * Hace consultas: llamar fuera del EDT.
 */
@Service
public class EmpresaService {

    private static final Logger LOG = LoggerFactory.getLogger(EmpresaService.class);
    static final int MAX_NIT = 20;
    static final int MAX_NOMBRE = 150;
    public static final String MENSAJE_NO_DISPONIBLE =
            "La empresa ya no existe o fue eliminada. Se actualizará la lista.";

    private final EmpresaDAO dao;
    private final SesionUsuario sesion;

    public EmpresaService(EmpresaDAO dao, SesionUsuario sesion) {
        this.dao = dao;
        this.sesion = sesion;
    }

    public List<EmpresaResumen> listar() {
        UsuarioSesion u = sesion.requerir();
        try {
            return dao.listarActivasPorUsuario(u.id()).stream().map(EmpresaService::resumen).toList();
        } catch (DataAccessException e) {
            throw traducir(e, "listar");
        }
    }

    /** Crea una empresa del usuario de la sesión y devuelve su ID. */
    public int crear(EmpresaFormulario datos) {
        UsuarioSesion u = sesion.requerir();
        EmpresaFormulario limpio = validar(datos);
        try {
            int id = dao.insertar(u.id(), limpio.nit(), limpio.nombre());
            LOG.info("Empresa creada: id={}, usuario={}", id, u.id());
            return id;
        } catch (DuplicateKeyException e) {
            throw nitDuplicado(limpio.nit());
        } catch (DataAccessException e) {
            throw traducir(e, "crear");
        }
    }

    public void actualizar(int id, EmpresaFormulario datos) {
        UsuarioSesion u = sesion.requerir();
        EmpresaFormulario limpio = validar(datos);
        int filas;
        try {
            filas = dao.actualizar(id, u.id(), limpio.nit(), limpio.nombre());
        } catch (DuplicateKeyException e) {
            throw nitDuplicado(limpio.nit());
        } catch (DataAccessException e) {
            throw traducir(e, "actualizar");
        }
        if (filas == 0) {
            throw new AplicacionException(MENSAJE_NO_DISPONIBLE);
        }
        LOG.info("Empresa actualizada: id={}, usuario={}", id, u.id());
    }

    /** Borrado lógico: fija eliminado_en; la fila, su NIT y sus referencias se conservan. */
    public void eliminar(int id) {
        UsuarioSesion u = sesion.requerir();
        int filas;
        try {
            filas = dao.marcarEliminada(id, u.id());
        } catch (DataAccessException e) {
            throw traducir(e, "eliminar");
        }
        if (filas == 0) {
            throw new AplicacionException(MENSAJE_NO_DISPONIBLE);
        }
        LOG.info("Empresa eliminada (lógico): id={}, usuario={}", id, u.id());
    }

    /** Recorta espacios y valida obligatoriedad y longitud (en caracteres, como VARCHAR). */
    static EmpresaFormulario validar(EmpresaFormulario datos) {
        String nit = datos == null || datos.nit() == null ? "" : datos.nit().strip();
        String nombre = datos == null || datos.nombre() == null ? "" : datos.nombre().strip();
        List<String> errores = new ArrayList<>();
        if (nit.isEmpty()) {
            errores.add("El NIT es obligatorio.");
        } else if (nit.codePointCount(0, nit.length()) > MAX_NIT) {
            errores.add("El NIT admite como máximo " + MAX_NIT + " caracteres.");
        }
        if (nombre.isEmpty()) {
            errores.add("El nombre es obligatorio.");
        } else if (nombre.codePointCount(0, nombre.length()) > MAX_NOMBRE) {
            errores.add("El nombre admite como máximo " + MAX_NOMBRE + " caracteres.");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        return new EmpresaFormulario(nit, nombre);
    }

    /** La BD ya rechazó el duplicado (UNIQUE); aquí solo se explica el motivo. */
    private AplicacionException nitDuplicado(String nit) {
        Optional<Empresa> existente;
        try {
            existente = dao.buscarPorNit(nit);
        } catch (DataAccessException e) {
            existente = Optional.empty();
        }
        if (existente.isPresent() && !existente.get().activa()) {
            return new AplicacionException("El NIT " + nit + " pertenece a una empresa eliminada. "
                    + "Los NIT no se reutilizan y la empresa no se restaura automáticamente.");
        }
        return new AplicacionException("Ya existe una empresa registrada con el NIT " + nit + ".");
    }

    private static AplicacionException traducir(DataAccessException e, String operacion) {
        LOG.warn("Empresas: error de base de datos al {} ({})", operacion, e.getClass().getSimpleName());
        // Incluye CannotGetJdbcConnectionException (subclase).
        if (e instanceof DataAccessResourceFailureException) {
            return new AplicacionException(AutenticacionService.MENSAJE_BD_NO_DISPONIBLE, e);
        }
        return new AplicacionException("No se pudo completar la operación en la base de datos.", e);
    }

    private static EmpresaResumen resumen(Empresa e) {
        return new EmpresaResumen(e.id(), e.nit(), e.nombre(), e.creadoEn(), e.actualizadoEn());
    }
}
