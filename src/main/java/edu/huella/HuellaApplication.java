package edu.huella;

import edu.huella.config.DiagnosticoConexion;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/** Punto de entrada: contexto Spring sin web y con AWT no headless. */
@SpringBootApplication
public class HuellaApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext contexto = new SpringApplicationBuilder(HuellaApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run(args);

        // Puerta E2 (provisional): consulta fuera del EDT y ventana en el EDT.
        String resultado = contexto.getBean(DiagnosticoConexion.class).resumen();
        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("Huella de carbono — diagnóstico");
            ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            ventana.add(new JLabel("<html>" + resultado.replace("\n", "<br>") + "</html>",
                    SwingConstants.CENTER), BorderLayout.CENTER);
            ventana.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    contexto.close();
                }
            });
            ventana.setSize(520, 220);
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }
}
