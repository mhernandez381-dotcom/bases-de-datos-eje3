package edu.huella.service;

import edu.huella.dao.UsuarioDAO;
import edu.huella.dto.UsuarioSesion;
import edu.huella.exception.AplicacionException;
import edu.huella.exception.CredencialesInvalidasException;
import edu.huella.model.Usuario;
import edu.huella.session.SesionUsuario;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Login y logout. Hace consultas y BCrypt: llamar SIEMPRE fuera del EDT.
 * Nunca registra contraseñas ni hashes.
 */
@Service
public class AutenticacionService {

    private static final Logger LOG = LoggerFactory.getLogger(AutenticacionService.class);
    /** BCrypt solo usa los primeros 72 bytes; contraseñas más largas se rechazan. */
    static final int MAX_BYTES_PASSWORD = 72;
    public static final String MENSAJE_BD_NO_DISPONIBLE =
            "No se pudo conectar con la base de datos. Inténtelo de nuevo en unos momentos.";

    private final UsuarioDAO usuarioDao;
    private final PasswordEncoder encoder;
    private final SesionUsuario sesion;
    /** Hash ficticio para que un correo inexistente tarde lo mismo que uno existente. */
    private volatile String hashFicticio;

    public AutenticacionService(UsuarioDAO usuarioDao, PasswordEncoder encoder, SesionUsuario sesion) {
        this.usuarioDao = usuarioDao;
        this.encoder = encoder;
        this.sesion = sesion;
    }

    /**
     * Valida credenciales de un usuario ACTIVO e inicia la sesión.
     *
     * @throws CredencialesInvalidasException mensaje genérico (no revela si el correo existe)
     * @throws AplicacionException            campos vacíos o base de datos no disponible
     */
    public UsuarioSesion iniciarSesion(String correo, char[] password) {
        String correoNormalizado = Correos.normalizar(correo);
        if (correoNormalizado.isEmpty() || password == null || password.length == 0) {
            throw new AplicacionException("Escriba el correo y la contraseña.");
        }
        CharBuffer clave = CharBuffer.wrap(password);
        if (StandardCharsets.UTF_8.encode(clave.duplicate()).remaining() > MAX_BYTES_PASSWORD) {
            throw new CredencialesInvalidasException();
        }

        Optional<Usuario> usuario;
        try {
            usuario = usuarioDao.buscarActivoPorCorreo(correoNormalizado);
        } catch (DataAccessException e) {
            LOG.warn("Login: base de datos no disponible ({})", e.getClass().getSimpleName());
            throw new AplicacionException(MENSAJE_BD_NO_DISPONIBLE, e);
        }

        if (usuario.isEmpty()) {
            encoder.matches(clave, hashFicticio());
            LOG.info("Login rechazado: credenciales inválidas.");
            throw new CredencialesInvalidasException();
        }
        Usuario u = usuario.get();
        if (!encoder.matches(clave, u.passwordHash())) {
            LOG.info("Login rechazado: credenciales inválidas.");
            throw new CredencialesInvalidasException();
        }

        UsuarioSesion datosSesion = new UsuarioSesion(u.id(), u.nombre(), u.correo());
        sesion.iniciar(datosSesion);
        LOG.info("Sesión iniciada: usuario id={}", u.id());
        return datosSesion;
    }

    public void cerrarSesion() {
        sesion.actual().ifPresent(u -> LOG.info("Sesión cerrada: usuario id={}", u.id()));
        sesion.cerrar();
    }

    private String hashFicticio() {
        String h = hashFicticio;
        if (h == null) {
            h = encoder.encode("sin-usuario-" + System.nanoTime());
            hashFicticio = h;
        }
        return h;
    }
}
