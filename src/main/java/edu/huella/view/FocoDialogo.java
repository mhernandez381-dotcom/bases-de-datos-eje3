package edu.huella.view;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.Timer;

/**
 * Algunos gestores de ventanas (fluxbox en el escritorio noVNC) muestran un diálogo modal
 * sin darle el foco de teclado: Enter/Escape/atajos se pierden hasta hacer clic en él.
 * Al mostrarse, se pide el foco y se reintenta unas pocas veces hasta que lo obtiene.
 */
final class FocoDialogo {

    private static final int INTENTOS = 6;
    private static final int PAUSA_MS = 120;

    private FocoDialogo() {
    }

    /** Instala el comportamiento; {@code destino} es el componente que debe recibir el foco. */
    static void asegurar(Window dialogo, Component destino, Runnable alEnfocar) {
        dialogo.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                int[] restantes = {INTENTOS};
                Timer reintento = new Timer(PAUSA_MS, null);
                reintento.addActionListener(ev -> {
                    if (!dialogo.isShowing()) {
                        reintento.stop();
                        return;
                    }
                    if (dialogo.isFocused()) {
                        reintento.stop();
                        return;
                    }
                    dialogo.toFront();
                    destino.requestFocus();
                    if (--restantes[0] <= 0) {
                        reintento.stop();
                    }
                });
                reintento.setInitialDelay(0);
                reintento.start();
                if (alEnfocar != null) {
                    alEnfocar.run();
                }
            }
        });
    }
}
