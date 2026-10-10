
package controlador;

import dao.CategoriaDAO;
import modelo.Categoria;
import vista.VentanaCategorias;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class CategoriaControlador {

    private final CategoriaDAO categoriaDAO =
            new CategoriaDAO();

    private final VentanaCategorias vista;

    private List<Categoria> categoriasActuales = List.of();

    public CategoriaControlador(JFrame padre) {

        vista = new VentanaCategorias(padre);

        vista.setAccionRegistrar(e -> registrarCategoria());

        vista.setAccionModificar(e -> modificarCategoria());

        vista.setAccionEliminar(e -> eliminarCategoria());

        vista.setAccionActualizar(e -> cargarCategorias());
    }

    public void mostrar() {

        cargarCategorias();
        vista.setVisible(true);
    }

    private void cargarCategorias() {

        vista.setProcesando(true);

        new SwingWorker<List<Categoria>, Void>() {

            @Override
            protected List<Categoria> doInBackground()
                    throws Exception {

                return categoriaDAO.listar();
            }

            @Override
            protected void done() {

                vista.setProcesando(false);

                try {

                    categoriasActuales = get();

                    vista.mostrarCategorias(
                            categoriasActuales
                    );

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    vista.mostrarEstado(
                            "Consulta interrumpida."
                    );

                } catch (ExecutionException ex) {

                    mostrarError(
                            "No se pudieron cargar las categorías."
                    );

                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private Categoria obtenerCategoriaSeleccionada() {

        Integer id = vista.getIdCategoriaSeleccionada();

        if (id == null) {

            mostrarError(
                    "Seleccione primero una categoría."
            );

            return null;
        }

        for (Categoria categoria : categoriasActuales) {

            if (categoria.getId() == id) {
                return categoria;
            }
        }

        mostrarError("No se encontró la categoría seleccionada.");

        return null;
    }

    private void registrarCategoria() {

        String nombre = vista.solicitarNombre(
                "Registrar categoría", ""
        );

        if (nombre == null) {
            return;
        }

        Categoria categoria = new Categoria(nombre);

        ejecutarOperacion(
                () -> categoriaDAO.crear(categoria) > 0,
                "Categoría registrada correctamente."
        );
    }

    private void modificarCategoria() {

        Categoria original =
                obtenerCategoriaSeleccionada();

        if (original == null) {
            return;
        }

        String nombre = vista.solicitarNombre(
                "Modificar categoría",
                original.getNombre()
        );

        if (nombre == null) {
            return;
        }

        Categoria actualizada = new Categoria(
                original.getId(),
                nombre
        );

        ejecutarOperacion(
                () -> categoriaDAO.actualizar(actualizada),
                "Categoría modificada correctamente."
        );
    }

    private void eliminarCategoria() {

        Categoria categoria =
                obtenerCategoriaSeleccionada();

        if (categoria == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                vista,
                "¿Desea eliminar la categoría \""
                        + categoria.getNombre() + "\"?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutarOperacion(
                () -> categoriaDAO.eliminar(
                        categoria.getId()
                ),
                "Categoría eliminada correctamente."
        );
    }

    private void ejecutarOperacion(
            Callable<Boolean> operacion,
            String mensajeExito) {

        vista.setProcesando(true);

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                return operacion.call();
            }

            @Override
            protected void done() {

                vista.setProcesando(false);

                try {

                    boolean correcto = get();

                    if (correcto) {

                        JOptionPane.showMessageDialog(
                                vista,
                                mensajeExito
                        );

                        cargarCategorias();

                    } else {

                        mostrarError(
                                "No se encontró el registro."
                        );
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    vista.mostrarEstado(
                            "Operación interrumpida."
                    );

                } catch (ExecutionException ex) {

                    Throwable causa = ex.getCause();

                    String mensaje =
                            "No se pudo completar la operación.";

                    if (causa instanceof SQLException sqlEx) {

                        if (sqlEx.getErrorCode() == 1062) {

                            mensaje =
                                    "Ya existe una categoría con ese nombre.";

                        } else if (sqlEx.getErrorCode() == 1451) {

                            mensaje = """
                                    No se puede eliminar esta categoría
                                    porque tiene libros asociados.
                                    """;

                        } else if (
                                "23000".equals(sqlEx.getSQLState())) {

                            mensaje = """
                                    La operación incumple una restricción
                                    de la base de datos.
                                    """;
                        }
                    }

                    mostrarError(mensaje);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void mostrarError(String mensaje) {

        JOptionPane.showMessageDialog(
                vista,
                mensaje,
                "Aviso",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
