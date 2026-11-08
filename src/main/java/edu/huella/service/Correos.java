package edu.huella.service;

import java.util.Locale;

/** Política única de normalización de correos: sin espacios extremos y en minúsculas. */
public final class Correos {

    private Correos() {
    }

    public static String normalizar(String correo) {
        return correo == null ? "" : correo.strip().toLowerCase(Locale.ROOT);
    }
}
