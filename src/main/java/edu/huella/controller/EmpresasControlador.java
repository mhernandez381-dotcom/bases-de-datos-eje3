package edu.huella.controller;

import edu.huella.service.AutenticacionService;
import edu.huella.view.EmpresasVista;

/** Acciones de la ventana principal. En E3 solo cierre de sesión; el CRUD llega en E4. */
public class EmpresasControlador {

    private final AutenticacionService autenticacion;
    private final Runnable alCerrarSesion;

    public EmpresasControlador(EmpresasVista vista, AutenticacionService autenticacion,
                               Runnable alCerrarSesion) {
        this.autenticacion = autenticacion;
        this.alCerrarSesion = alCerrarSesion;
        vista.alCerrarSesion(e -> cerrarSesion());
    }

    private void cerrarSesion() {
        autenticacion.cerrarSesion(); // solo memoria: no bloquea el EDT
        alCerrarSesion.run();
    }
}
