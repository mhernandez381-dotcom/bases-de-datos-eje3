package edu.huella.controller;

import edu.huella.dto.UsuarioSesion;
import edu.huella.exception.AplicacionException;
import edu.huella.service.AutenticacionService;
import edu.huella.view.LoginVista;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Conecta la vista de login con el servicio. BD y BCrypt en SwingWorker, nunca en el EDT. */
public class LoginControlador {

    private static final Logger LOG = LoggerFactory.getLogger(LoginControlador.class);

    private final LoginVista vista;
    private final AutenticacionService autenticacion;
    private final Consumer<UsuarioSesion> alAutenticar;
    private boolean enCurso;

    public LoginControlador(LoginVista vista, AutenticacionService autenticacion,
                            Consumer<UsuarioSesion> alAutenticar) {
        this.vista = vista;
        this.autenticacion = autenticacion;
        this.alAutenticar = alAutenticar;
        vista.alEntrar(e -> entrar());
    }

    /** Se ejecuta en el EDT. */
    private void entrar() {
        if (enCurso) {
            return; // doble clic / doble Enter
        }
        enCurso = true;
        String correo = vista.correo();
        char[] password = vista.password();
        vista.limpiarPassword();
        vista.setOcupado(true);

        new SwingWorker<UsuarioSesion, Void>() {
            @Override
            protected UsuarioSesion doInBackground() {
                try {
                    return autenticacion.iniciarSesion(correo, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                enCurso = false;
                vista.setOcupado(false);
                try {
                    alAutenticar.accept(get());
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof AplicacionException ae) {
                        vista.mostrarError(ae.getMessage());
                    } else {
                        LOG.error("Error inesperado en el login", e.getCause());
                        vista.mostrarError("Ocurrió un error inesperado. Inténtelo de nuevo.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
