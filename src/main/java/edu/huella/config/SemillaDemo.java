package edu.huella.config;

import edu.huella.dao.UsuarioDAO;
import edu.huella.service.Correos;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cuenta demo, SOLO con el perfil "demo". Idempotente: si el correo ya existe
 * (activo o eliminado) no la modifica, no cambia el hash y no la reactiva.
 * Datos desde HUELLA_DEMO_NOMBRE / HUELLA_DEMO_CORREO / HUELLA_DEMO_PASSWORD.
 */
@Component
@Profile("demo")
public class SemillaDemo implements ApplicationRunner {

    public enum Resultado { CREADA, YA_EXISTIA, BD_NO_DISPONIBLE }

    private static final Logger LOG = LoggerFactory.getLogger(SemillaDemo.class);

    private final UsuarioDAO usuarioDao;
    private final PasswordEncoder encoder;
    private final String nombre;
    private final String correo;
    private final String password;

    public SemillaDemo(UsuarioDAO usuarioDao, PasswordEncoder encoder,
                       @Value("${huella.demo.nombre:}") String nombre,
                       @Value("${huella.demo.correo:}") String correo,
                       @Value("${huella.demo.password:}") String password) {
        this.usuarioDao = usuarioDao;
        this.encoder = encoder;
        this.nombre = nombre == null ? "" : nombre.strip();
        this.correo = Correos.normalizar(correo);
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        aplicar();
    }

    /** @throws IllegalStateException si la configuración demo es inválida (se activó explícitamente). */
    public Resultado aplicar() {
        validarConfiguracion();
        try {
            if (usuarioDao.buscarPorCorreo(correo).isPresent()) {
                LOG.info("Semilla demo: la cuenta ya existe; no se modifica.");
                return Resultado.YA_EXISTIA;
            }
            int id = usuarioDao.insertar(nombre, correo, encoder.encode(password));
            LOG.info("Semilla demo: cuenta creada (id={}).", id);
            return Resultado.CREADA;
        } catch (DuplicateKeyException e) {
            LOG.info("Semilla demo: la cuenta ya existe (creada en paralelo); no se modifica.");
            return Resultado.YA_EXISTIA;
        } catch (DataAccessException e) {
            LOG.warn("Semilla demo no aplicada: base de datos no disponible ({}).", e.getClass().getSimpleName());
            return Resultado.BD_NO_DISPONIBLE;
        }
    }

    private void validarConfiguracion() {
        if (nombre.isEmpty() || nombre.length() > 100) {
            throw new IllegalStateException("Perfil demo: HUELLA_DEMO_NOMBRE vacío o mayor de 100 caracteres.");
        }
        if (correo.isEmpty() || correo.length() > 150 || !correo.contains("@")) {
            throw new IllegalStateException("Perfil demo: HUELLA_DEMO_CORREO vacío, inválido o mayor de 150 caracteres.");
        }
        int bytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (password.length() < 8 || bytes > 72) {
            throw new IllegalStateException("Perfil demo: HUELLA_DEMO_PASSWORD debe tener entre 8 caracteres y 72 bytes.");
        }
    }
}
