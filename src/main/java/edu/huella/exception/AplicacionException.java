package edu.huella.exception;

/**
 * Error de aplicación cuyo mensaje es seguro para mostrarlo al usuario
 * (sin SQL, trazas ni datos sensibles). El detalle técnico va en la causa y en el log.
 */
public class AplicacionException extends RuntimeException {

    public AplicacionException(String mensajeParaUsuario) {
        super(mensajeParaUsuario);
    }

    public AplicacionException(String mensajeParaUsuario, Throwable causa) {
        super(mensajeParaUsuario, causa);
    }
}
