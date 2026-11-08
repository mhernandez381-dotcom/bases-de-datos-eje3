package edu.huella.view;

import edu.huella.dto.EmpresaResumen;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Modelo de solo lectura para la tabla de empresas. */
public class EmpresasTablaModelo extends AbstractTableModel {

    /** Formato documentado; la zona se indica en la cabecera (las fechas vienen en UTC). */
    static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] COLUMNAS = {"ID", "NIT", "Nombre", "Creada (UTC)", "Actualizada (UTC)"};

    private List<EmpresaResumen> filas = new ArrayList<>();

    public void setFilas(List<EmpresaResumen> nuevas) {
        filas = new ArrayList<>(nuevas);
        fireTableDataChanged();
    }

    public EmpresaResumen fila(int indiceModelo) {
        return filas.get(indiceModelo);
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        return columna == 0 ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        EmpresaResumen e = filas.get(fila);
        return switch (columna) {
            case 0 -> e.id();
            case 1 -> e.nit();
            case 2 -> e.nombre();
            case 3 -> formatear(e.creadoEn());
            case 4 -> formatear(e.actualizadoEn());
            default -> throw new IndexOutOfBoundsException(columna);
        };
    }

    private static String formatear(LocalDateTime fecha) {
        return fecha == null ? "" : FORMATO.format(fecha);
    }
}
