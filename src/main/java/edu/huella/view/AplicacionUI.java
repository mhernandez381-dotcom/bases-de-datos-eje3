package edu.huella.view;

import edu.huella.controller.EmpresasControlador;
import edu.huella.controller.LoginControlador;
import edu.huella.dto.UsuarioSesion;
import edu.huella.service.AutenticacionService;
import edu.huella.service.EmpresaService;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Navegación entre ventanas (login ↔ empresas) y cierre de la aplicación.
 * Todos los métodos se ejecutan en el EDT.
 */
public class AplicacionUI {

    private final ConfigurableApplicationContext contexto;
    private final AutenticacionService autenticacion;
    private final EmpresaService empresas;
    private JFrame ventanaActual;
    private boolean cerrando;

    public AplicacionUI(ConfigurableApplicationContext contexto, AutenticacionService autenticacion,
                        EmpresaService empresas) {
        this.contexto = contexto;
        this.autenticacion = autenticacion;
        this.empresas = empresas;
    }

    public void mostrarLogin() {
        assert SwingUtilities.isEventDispatchThread();
        if (cerrando) {
            return;
        }
        LoginVista vista = new LoginVista();
        new LoginControlador(vista, autenticacion, this::mostrarEmpresas);
        vista.alCerrar(this::salir);
        cambiarA(vista);
        vista.enfocarInicio();
    }

    private void mostrarEmpresas(UsuarioSesion usuario) {
        if (cerrando) {
            return; // el usuario cerró la ventana mientras se verificaba
        }
        EmpresasVista vista = new EmpresasVista(usuario);
        EmpresasControlador controlador = new EmpresasControlador(vista, empresas, autenticacion, this::mostrarLogin);
        vista.alCerrar(this::salir);
        cambiarA(vista);
        controlador.iniciar();
    }

    private void cambiarA(JFrame nueva) {
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
        ventanaActual = nueva;
        nueva.setVisible(true);
    }

    /** Cierra la ventana, la sesión y el contexto Spring (pool de conexiones) para que la JVM termine. */
    private void salir() {
        if (cerrando) {
            return;
        }
        cerrando = true;
        autenticacion.cerrarSesion();
        if (ventanaActual != null) {
            ventanaActual.dispose();
            ventanaActual = null;
        }
        Thread cierre = new Thread(contexto::close, "cierre-contexto");
        cierre.start();
    }
}
