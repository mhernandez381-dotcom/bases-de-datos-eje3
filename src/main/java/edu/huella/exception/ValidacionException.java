package edu.huella.exception;

import java.util.List;

/** Datos de formulario inválidos; se detecta antes de tocar la base de datos. */
public class ValidacionException extends AplicacionException {

    private final List<String> errores;

    public ValidacionException(List<String> errores) {
        super(String.join(" ", errores));
        this.errores = List.copyOf(errores);
    }

    public List<String> errores() {
        return errores;
    }
}
