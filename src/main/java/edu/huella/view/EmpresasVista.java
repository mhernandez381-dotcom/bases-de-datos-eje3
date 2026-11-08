package edu.huella.view;

import edu.huella.dto.UsuarioSesion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Ventana principal tras el login. En E3 solo muestra el usuario de la sesión y
 * permite cerrar sesión; la tabla y las acciones del CRUD se añaden en E4.
 */
public class EmpresasVista extends JFrame {

    private final JButton cerrarSesion = new JButton("Cerrar sesión");
    private final JPanel contenido = new JPanel(new BorderLayout());

    public EmpresasVista(UsuarioSesion usuario) {
        super("Huella de carbono — Empresas");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        cabecera.add(new JLabel("Sesión: " + usuario.nombre() + " (" + usuario.correo() + ")"),
                BorderLayout.CENTER);
        cerrarSesion.setMnemonic(KeyEvent.VK_S);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(cerrarSesion);
        cabecera.add(derecha, BorderLayout.LINE_END);

        contenido.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        contenido.add(new JLabel("Gestión de empresas (pendiente: etapa E4).", SwingConstants.CENTER),
                BorderLayout.CENTER);

        add(cabecera, BorderLayout.PAGE_START);
        add(contenido, BorderLayout.CENTER);
        setSize(760, 480);
        setMinimumSize(new java.awt.Dimension(520, 320));
        setLocationRelativeTo(null);
    }

    public void alCerrarSesion(ActionListener accion) {
        cerrarSesion.addActionListener(accion);
    }

    public void alCerrar(Runnable accion) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                accion.run();
            }
        });
    }
}
