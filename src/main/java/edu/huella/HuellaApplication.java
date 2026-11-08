package edu.huella;

import edu.huella.service.AutenticacionService;
import edu.huella.view.AplicacionUI;
import javax.swing.SwingUtilities;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/** Punto de entrada: contexto Spring sin web y con AWT no headless; la UI se crea en el EDT. */
@SpringBootApplication
public class HuellaApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext contexto = new SpringApplicationBuilder(HuellaApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run(args);
        AutenticacionService autenticacion = contexto.getBean(AutenticacionService.class);
        SwingUtilities.invokeLater(() -> new AplicacionUI(contexto, autenticacion).mostrarLogin());
    }
}
