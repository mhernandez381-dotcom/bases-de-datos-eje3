package edu.huella.session;

import edu.huella.dto.UsuarioSesion;
import edu.huella.exception.AplicacionException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/** Sesión en memoria del único proceso de escritorio. Se lee desde el EDT y desde hilos de trabajo. */
@Component
public class SesionUsuario {

    private final AtomicReference<UsuarioSesion> actual = new AtomicReference<>();

    public void iniciar(UsuarioSesion usuario) {
        actual.set(usuario);
    }

    public void cerrar() {
        actual.set(null);
    }

    public Optional<UsuarioSesion> actual() {
        return Optional.ofNullable(actual.get());
    }

    /** Usuario autenticado o error si la sesión está cerrada (no se aceptan IDs desde formularios). */
    public UsuarioSesion requerir() {
        UsuarioSesion u = actual.get();
        if (u == null) {
            throw new AplicacionException("La sesión no está iniciada. Inicie sesión de nuevo.");
        }
        return u;
    }
}
