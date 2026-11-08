package edu.huella.controller;

import edu.huella.dto.EmpresaFormulario;
import edu.huella.dto.EmpresaResumen;
import edu.huella.exception.AplicacionException;
import edu.huella.service.AutenticacionService;
import edu.huella.service.EmpresaService;
import edu.huella.view.EmpresaFormularioDialogo;
import edu.huella.view.EmpresasVista;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conecta la ventana de empresas con el servicio. Cada operación de BD va en un SwingWorker;
 * mientras hay una en curso se ignoran nuevas acciones (evita duplicados por doble clic).
 * Todos los métodos se ejecutan en el EDT.
 */
public class EmpresasControlador {

    private static final Logger LOG = LoggerFactory.getLogger(EmpresasControlador.class);
    private static final String ERROR_INESPERADO = "Ocurrió un error inesperado. Inténtelo de nuevo.";

    private final EmpresasVista vista;
    private final EmpresaFormularioDialogo formulario;
    private final EmpresaService empresas;
    private final AutenticacionService autenticacion;
    private final Runnable alCerrarSesion;
    private boolean ocupado;
    /** null = alta; otro valor = ID de la empresa en edición. */
    private Integer idEnEdicion;

    public EmpresasControlador(EmpresasVista vista, EmpresaService empresas,
                               AutenticacionService autenticacion, Runnable alCerrarSesion) {
        this.vista = vista;
        this.formulario = vista.formulario();
        this.empresas = empresas;
        this.autenticacion = autenticacion;
        this.alCerrarSesion = alCerrarSesion;
        vista.alNueva(e -> nueva());
        vista.alEditar(e -> editar());
        vista.alEliminar(e -> eliminar());
        vista.alActualizar(e -> cargar("Lista actualizada.", false, null));
        vista.alCerrarSesion(e -> cerrarSesion());
        formulario.alGuardar(this::guardar);
    }

    /** Primera carga, al mostrar la ventana. */
    public void iniciar() {
        ejecutar("Cargando…", empresas::listar, lista -> {
            vista.mostrarEmpresas(lista);
            vista.mostrarInfo(textoCantidad(lista));
            vista.enfocarTabla();
        }, vista::mostrarError);
    }

    /** Recarga la lista; opcionalmente muestra un mensaje y selecciona un ID. */
    private void cargar(String mensajeFinal, boolean esError, Integer idSeleccionar) {
        ejecutar("Cargando…", empresas::listar, lista -> {
            vista.mostrarEmpresas(lista);
            if (idSeleccionar != null) {
                vista.seleccionarId(idSeleccionar);
            }
            if (mensajeFinal != null) {
                mostrar(mensajeFinal, esError);
            } else {
                vista.mostrarInfo(textoCantidad(lista));
            }
        }, error -> vista.mostrarError(error));
    }

    private void nueva() {
        if (ocupado) {
            return;
        }
        idEnEdicion = null;
        formulario.abrirNueva();
    }

    private void editar() {
        if (ocupado) {
            return;
        }
        vista.seleccionada().ifPresent(e -> {
            idEnEdicion = e.id();
            formulario.abrirEdicion(e);
        });
    }

    private void guardar(EmpresaFormulario datos) {
        if (ocupado) {
            return;
        }
        Integer id = idEnEdicion;
        formulario.setOcupado(true);
        ejecutar(null, () -> {
            if (id == null) {
                return empresas.crear(datos);
            }
            empresas.actualizar(id, datos);
            return id;
        }, idGuardado -> {
            formulario.setOcupado(false);
            formulario.cerrar();
            cargar(id == null ? "Empresa creada." : "Empresa actualizada.", false, idGuardado);
        }, error -> {
            formulario.setOcupado(false);
            if (EmpresaService.MENSAJE_NO_DISPONIBLE.equals(error)) {
                formulario.cerrar();
                cargar(error, true, null);
            } else {
                formulario.mostrarError(error); // el formulario sigue abierto para corregir
            }
        });
    }

    private void eliminar() {
        if (ocupado) {
            return;
        }
        EmpresaResumen e = vista.seleccionada().orElse(null);
        if (e == null || !vista.confirmarEliminacion(e)) {
            return;
        }
        ejecutar("Eliminando…", () -> {
            empresas.eliminar(e.id());
            return e.id();
        }, id -> cargar("Empresa «" + e.nombre() + "» eliminada.", false, null), error -> {
            if (EmpresaService.MENSAJE_NO_DISPONIBLE.equals(error)) {
                cargar(error, true, null);
            } else {
                vista.mostrarError(error);
            }
        });
    }

    private void cerrarSesion() {
        if (ocupado) {
            return;
        }
        autenticacion.cerrarSesion(); // solo memoria
        alCerrarSesion.run();
    }

    // ---- Infraestructura ----

    private <T> void ejecutar(String textoOcupado, Supplier<T> tarea, Consumer<T> exito, Consumer<String> fallo) {
        ocupado = true;
        vista.setOcupado(true, textoOcupado);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return tarea.get();
            }

            @Override
            protected void done() {
                ocupado = false;
                vista.setOcupado(false, null);
                try {
                    exito.accept(get());
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof AplicacionException ae) {
                        fallo.accept(ae.getMessage());
                    } else {
                        LOG.error("Error inesperado en empresas", ex.getCause());
                        fallo.accept(ERROR_INESPERADO);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void mostrar(String texto, boolean esError) {
        if (esError) {
            vista.mostrarError(texto);
        } else {
            vista.mostrarInfo(texto);
        }
    }

    private static String textoCantidad(List<EmpresaResumen> lista) {
        return lista.size() == 1 ? "1 empresa." : lista.size() + " empresas.";
    }
}
