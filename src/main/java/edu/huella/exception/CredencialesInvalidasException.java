package edu.huella.exception;

/** Credenciales incorrectas o usuario inactivo: mismo mensaje genérico en todos los casos. */
public class CredencialesInvalidasException extends AplicacionException {

    public static final String MENSAJE = "Correo o contraseña incorrectos.";

    public CredencialesInvalidasException() {
        super(MENSAJE);
    }
}
