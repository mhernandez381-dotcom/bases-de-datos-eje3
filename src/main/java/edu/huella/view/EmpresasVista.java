package edu.huella.view;

import edu.huella.dto.EmpresaResumen;
import edu.huella.dto.UsuarioSesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/** Ventana principal: tabla de empresas activas del usuario y acciones del CRUD. */
public class EmpresasVista extends JFrame {

    private final EmpresasTablaModelo modelo = new EmpresasTablaModelo();
    private final JTable tabla = new JTable(modelo);
    private final JButton nuevo = new JButton("Nueva");
    private final JButton editar = new JButton("Editar");
    private final JButton eliminar = new JButton("Eliminar");
    private final JButton actualizar = new JButton("Actualizar");
    private final JButton cerrarSesion = new JButton("Cerrar sesión");
    private final JLabel estado = new JLabel(" ");
    private final EmpresaFormularioDialogo formulario;
    private boolean ocupado;

    public EmpresasVista(UsuarioSesion usuario) {
        super("Huella de carbono — Empresas");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        cabecera.add(new JLabel("Sesión: " + usuario.nombre() + " (" + usuario.correo() + ")"),
                BorderLayout.CENTER);
        cerrarSesion.setMnemonic(KeyEvent.VK_S);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(cerrarSesion);
        cabecera.add(derecha, BorderLayout.LINE_END);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nuevo.setMnemonic(KeyEvent.VK_N);
        editar.setMnemonic(KeyEvent.VK_E);
        eliminar.setMnemonic(KeyEvent.VK_L);
        actualizar.setMnemonic(KeyEvent.VK_A);
        nuevo.setToolTipText("Nueva empresa (Alt+N)");
        editar.setToolTipText("Editar la seleccionada (Alt+E o doble clic)");
        eliminar.setToolTipText("Eliminar la seleccionada (Alt+L o Supr)");
        actualizar.setToolTipText("Volver a cargar la lista (Alt+A o F5)");
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(eliminar);
        acciones.add(actualizar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(cabecera, BorderLayout.PAGE_START);
        norte.add(acciones, BorderLayout.PAGE_END);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabla.getSelectionModel().addListSelectionListener(e -> refrescarBotones());
        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 12, 0, 12), desplazamiento.getBorder()));

        estado.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));

        add(norte, BorderLayout.PAGE_START);
        add(desplazamiento, BorderLayout.CENTER);
        add(estado, BorderLayout.PAGE_END);

        formulario = new EmpresaFormularioDialogo(this);
        setSize(860, 520);
        setMinimumSize(new Dimension(560, 340));
        setLocationRelativeTo(null);
        refrescarBotones();
    }

    // ---- Eventos que conecta el controlador ----

    public void alNueva(ActionListener a) {
        nuevo.addActionListener(a);
    }

    public void alEditar(ActionListener a) {
        editar.addActionListener(a);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.rowAtPoint(e.getPoint()) >= 0 && editar.isEnabled()) {
                    editar.doClick();
                }
            }
        });
    }

    public void alEliminar(ActionListener a) {
        eliminar.addActionListener(a);
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar", eliminar);
    }

    public void alActualizar(ActionListener a) {
        actualizar.addActionListener(a);
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "actualizar", actualizar);
    }

    public void alCerrarSesion(ActionListener a) {
        cerrarSesion.addActionListener(a);
    }

    public void alCerrar(Runnable accion) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                accion.run();
            }
        });
    }

    // ---- Estado visual ----

    public EmpresaFormularioDialogo formulario() {
        return formulario;
    }

    public void mostrarEmpresas(List<EmpresaResumen> empresas) {
        Integer seleccion = seleccionada().map(EmpresaResumen::id).orElse(null);
        modelo.setFilas(empresas);
        if (seleccion != null) {
            seleccionarId(seleccion);
        }
        refrescarBotones();
    }

    public void seleccionarId(int id) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (modelo.fila(i).id() == id) {
                int vista = tabla.convertRowIndexToView(i);
                tabla.setRowSelectionInterval(vista, vista);
                tabla.scrollRectToVisible(tabla.getCellRect(vista, 0, true));
                return;
            }
        }
    }

    /** Carga inicial: foco en la tabla y primera fila seleccionada, para operar solo con teclado. */
    public void enfocarTabla() {
        if (tabla.getSelectedRow() < 0 && tabla.getRowCount() > 0) {
            tabla.setRowSelectionInterval(0, 0);
        }
        tabla.requestFocusInWindow();
    }

    public Optional<EmpresaResumen> seleccionada() {
        int vista = tabla.getSelectedRow();
        return vista < 0 ? Optional.empty() : Optional.of(modelo.fila(tabla.convertRowIndexToModel(vista)));
    }

    public void setOcupado(boolean valor, String texto) {
        ocupado = valor;
        nuevo.setEnabled(!valor);
        actualizar.setEnabled(!valor);
        cerrarSesion.setEnabled(!valor);
        refrescarBotones();
        if (texto != null) {
            mostrarInfo(texto);
        }
        if (!valor) {
            restaurarFoco();
        }
    }

    /**
     * Al desactivar el botón que tenía el foco (p. ej. «Nueva» durante la recarga), la ventana
     * se queda sin componente enfocado y los atajos Alt+… dejan de responder. Se devuelve a la tabla.
     */
    private void restaurarFoco() {
        java.awt.Component foco = getFocusOwner();
        if (isActive() && (foco == null || !foco.isEnabled())) {
            tabla.requestFocusInWindow();
        }
    }

    public void mostrarInfo(String texto) {
        estado.setForeground(UIManager.getColor("Label.foreground"));
        estado.setText(texto);
    }

    public void mostrarError(String texto) {
        estado.setForeground(new Color(0xB0, 0x00, 0x20));
        estado.setText(texto);
    }

    public boolean confirmarEliminacion(EmpresaResumen e) {
        Object[] opciones = {"Eliminar", "Cancelar"};
        JOptionPane panel = new JOptionPane(
                "¿Eliminar la empresa «" + e.nombre() + "» (NIT " + e.nit() + ")?\n"
                        + "Dejará de aparecer en la lista. El NIT seguirá reservado y no podrá reutilizarse.",
                JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION, null, opciones, opciones[1]);
        JDialog dialogo = panel.createDialog(this, "Confirmar eliminación");
        // Algunos gestores de ventanas no dan el foco al diálogo modal (ver FocoDialogo).
        JButton cancelarBoton = buscarBoton(panel, (String) opciones[1]);
        FocoDialogo.asegurar(dialogo, cancelarBoton != null ? cancelarBoton : panel, null);
        dialogo.setVisible(true); // modal: bloquea hasta elegir
        dialogo.dispose();
        return opciones[0].equals(panel.getValue());
    }

    private static JButton buscarBoton(java.awt.Container raiz, String texto) {
        for (java.awt.Component c : raiz.getComponents()) {
            if (c instanceof JButton b && texto.equals(b.getText())) {
                return b;
            }
            if (c instanceof java.awt.Container hijo) {
                JButton encontrado = buscarBoton(hijo, texto);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private void refrescarBotones() {
        boolean hay = tabla.getSelectedRow() >= 0;
        editar.setEnabled(!ocupado && hay);
        eliminar.setEnabled(!ocupado && hay);
    }

    private void atajo(KeyStroke tecla, String nombre, JButton boton) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(tecla, nombre);
        getRootPane().getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (boton.isEnabled()) {
                    boton.doClick();
                }
            }
        });
    }
}
