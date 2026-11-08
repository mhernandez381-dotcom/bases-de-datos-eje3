package edu.huella.view;

import edu.huella.dto.EmpresaFormulario;
import edu.huella.dto.EmpresaResumen;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/** Formulario modal reutilizado para alta y edición. Solo NIT y nombre. */
public class EmpresaFormularioDialogo extends JDialog {

    private static final int ANCHO_MENSAJE_PX = 340;

    private final JTextField nit = new JTextField(20);
    private final JTextField nombre = new JTextField(36);
    private final JButton guardar = new JButton("Guardar");
    private final JButton cancelar = new JButton("Cancelar");
    private final JLabel mensaje = new JLabel(" ");
    private boolean ocupado;

    public EmpresaFormularioDialogo(JFrame propietario) {
        super(propietario, true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrarSiLibre();
            }
        });
        FocoDialogo.asegurar(this, nit, () -> {
            nit.requestFocusInWindow();
            nit.selectAll(); // en edición, escribir reemplaza en lugar de añadir
        });

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.LINE_START;

        JLabel etNit = new JLabel("NIT (máx. 20):");
        etNit.setDisplayedMnemonic(KeyEvent.VK_T);
        etNit.setLabelFor(nit);
        c.gridx = 0; c.gridy = 0;
        campos.add(etNit, c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        campos.add(nit, c);

        JLabel etNombre = new JLabel("Nombre (máx. 150):");
        etNombre.setDisplayedMnemonic(KeyEvent.VK_M);
        etNombre.setLabelFor(nombre);
        c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        campos.add(etNombre, c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        campos.add(nombre, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        campos.add(mensaje, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        guardar.setMnemonic(KeyEvent.VK_G);
        botones.add(guardar);
        botones.add(cancelar);
        cancelar.addActionListener(e -> cerrarSiLibre());

        add(campos, BorderLayout.CENTER);
        add(botones, BorderLayout.PAGE_END);
        getRootPane().setDefaultButton(guardar); // Enter = Guardar
        getRootPane().registerKeyboardAction(e -> cerrarSiLibre(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setMinimumSize(new Dimension(getWidth(), getHeight()));
    }

    public void alGuardar(Consumer<EmpresaFormulario> accion) {
        guardar.addActionListener(e -> accion.accept(new EmpresaFormulario(nit.getText(), nombre.getText())));
    }

    /** Muestra el diálogo (bloquea hasta que se cierre, como todo diálogo modal). */
    public void abrirNueva() {
        abrir("Nueva empresa", "", "");
    }

    public void abrirEdicion(EmpresaResumen empresa) {
        abrir("Editar empresa #" + empresa.id(), empresa.nit(), empresa.nombre());
    }

    private void abrir(String titulo, String valorNit, String valorNombre) {
        setTitle(titulo);
        nit.setText(valorNit);
        nombre.setText(valorNombre);
        limpiarMensaje();
        setOcupado(false);
        pack();
        setLocationRelativeTo(getOwner());
        nit.requestFocusInWindow();
        setVisible(true);
    }

    public void setOcupado(boolean valor) {
        ocupado = valor;
        nit.setEnabled(!valor);
        nombre.setEnabled(!valor);
        guardar.setEnabled(!valor);
        cancelar.setEnabled(!valor);
        if (valor) {
            mensaje.setForeground(UIManager.getColor("Label.foreground"));
            mensaje.setText("Guardando…");
        }
    }

    public void mostrarError(String texto) {
        mensaje.setForeground(new Color(0xB0, 0x00, 0x20));
        mensaje.setText("<html><body style='width:" + ANCHO_MENSAJE_PX + "px'>"
                + texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</body></html>");
        Dimension preferido = getPreferredSize();
        setSize(Math.max(getWidth(), preferido.width), Math.max(getHeight(), preferido.height));
        nit.requestFocusInWindow();
    }

    public void cerrar() {
        setVisible(false);
    }

    private void limpiarMensaje() {
        mensaje.setText(" ");
    }

    private void cerrarSiLibre() {
        if (!ocupado) {
            cerrar();
        }
    }
}
