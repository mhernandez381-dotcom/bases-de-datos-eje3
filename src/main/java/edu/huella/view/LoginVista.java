package edu.huella.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/** Pantalla de inicio de sesión. Solo componentes y estado visual: sin SQL ni lógica de negocio. */
public class LoginVista extends JFrame {

    private static final int ANCHO_MENSAJE_PX = 300;

    private final JTextField correo = new JTextField(24);
    private final JPasswordField password = new JPasswordField(24);
    private final JButton entrar = new JButton("Entrar");
    private final JLabel mensaje = new JLabel(" ");
    private final JProgressBar progreso = new JProgressBar();

    public LoginVista() {
        super("Huella de carbono — Iniciar sesión");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.LINE_START;

        JLabel titulo = new JLabel("Gestión de empresas");
        titulo.setFont(titulo.getFont().deriveFont(titulo.getFont().getSize2D() + 4f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        formulario.add(titulo, c);

        JLabel etiquetaCorreo = new JLabel("Correo:");
        etiquetaCorreo.setDisplayedMnemonic(KeyEvent.VK_C);
        etiquetaCorreo.setLabelFor(correo);
        c.gridy = 1; c.gridwidth = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        formulario.add(etiquetaCorreo, c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(correo, c);

        JLabel etiquetaPassword = new JLabel("Contraseña:");
        etiquetaPassword.setDisplayedMnemonic(KeyEvent.VK_O);
        etiquetaPassword.setLabelFor(password);
        c.gridx = 0; c.gridy = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        formulario.add(etiquetaPassword, c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(password, c);

        entrar.setMnemonic(KeyEvent.VK_E);
        c.gridx = 1; c.gridy = 3; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.LINE_END;
        formulario.add(entrar, c);

        progreso.setIndeterminate(true);
        progreso.setVisible(false);
        progreso.setPreferredSize(new Dimension(120, 12));
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(progreso, c);

        mensaje.setForeground(new Color(0xB0, 0x00, 0x20));
        c.gridy = 5;
        formulario.add(mensaje, c);

        add(formulario, BorderLayout.CENTER);
        getRootPane().setDefaultButton(entrar); // Enter = Entrar
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public void alEntrar(ActionListener accion) {
        entrar.addActionListener(accion);
    }

    /** La X de la ventana: el coordinador decide (cerrar la aplicación). */
    public void alCerrar(Runnable accion) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                accion.run();
            }
        });
    }

    public String correo() {
        return correo.getText();
    }

    /** Copia de la contraseña; quien la recibe debe borrarla con {@code Arrays.fill}. */
    public char[] password() {
        return password.getPassword();
    }

    public void limpiarPassword() {
        password.setText("");
    }

    /** Bloquea el formulario mientras se verifica (evita envíos dobles). */
    public void setOcupado(boolean ocupado) {
        correo.setEnabled(!ocupado);
        password.setEnabled(!ocupado);
        entrar.setEnabled(!ocupado);
        progreso.setVisible(ocupado);
        if (ocupado) {
            mensaje.setText("Verificando…");
            mensaje.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        }
    }

    public void mostrarError(String texto) {
        mensaje.setForeground(new Color(0xB0, 0x00, 0x20));
        // HTML con ancho fijo: el texto hace salto de línea en lugar de cortarse.
        mensaje.setText("<html><body style='width:" + ANCHO_MENSAJE_PX + "px'>" + escaparHtml(texto) + "</body></html>");
        Dimension preferido = getPreferredSize();
        setSize(Math.max(getWidth(), preferido.width), Math.max(getHeight(), preferido.height));
        if (correo.getText().isBlank()) {
            correo.requestFocusInWindow();
        } else {
            password.requestFocusInWindow();
        }
    }

    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void enfocarInicio() {
        correo.requestFocusInWindow();
    }
}
