
package controlador;

import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;

import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import modelo.Usuario;

import vista.VentanaPrestamos;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class PrestamoControlador {

    private final Usuario usuario;
    private final VentanaPrestamos vista;
    private final boolean bibliotecario;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final EstudianteDAO estudianteDAO =
            new EstudianteDAO();

    private List<Prestamo> prestamosActuales = List.of();

    private record Datos(
            List<Estudiante> estudiantes,
            List<Libro> libros,
            List<Prestamo> prestamos,
            Estudiante estudianteSesion) {
    }

    public PrestamoControlador(JFrame padre, Usuario usuario) {

        this.usuario = usuario;

        bibliotecario = "bibliotecario"
                .equalsIgnoreCase(usuario.getRol());

        vista = new VentanaPrestamos(padre, bibliotecario);

        vista.setAccionRegistrar(e -> registrarPrestamo());
        vista.setAccionDevolver(e -> devolverPrestamo());
        vista.setAccionActualizar(e -> cargarDatos());
    }

    public void mostrar() {
        cargarDatos();
        vista.setVisible(true);
    }

    private void cargarDatos() {

        vista.setProcesando(true);

        new SwingWorker<Datos, Void>() {

            @Override
            protected Datos doInBackground() throws Exception {

                List<Estudiante> estudiantes =
                        estudianteDAO.listar();

                List<Libro> libros = libroDAO.listar();

                Estudiante estudianteSesion = null;

                if (!bibliotecario) {

                    for (Estudiante estudiante : estudiantes) {

                        if (estudiante.getRut()
                                .equalsIgnoreCase(usuario.getRut())) {

                            estudianteSesion = estudiante;
                            break;
                        }
                    }
                }

                List<Prestamo> prestamos;

                if (bibliotecario) {
                    prestamos = prestamoDAO.listar();

                } else if (estudianteSesion != null) {
                    prestamos = prestamoDAO.listarPorEstudiante(
                            estudianteSesion.getId()
                    );

                } else {
                    prestamos = List.of();
                }

                return new Datos(
                        estudiantes, libros,
                        prestamos, estudianteSesion
                );
            }

            @Override
            protected void done() {

                vista.setProcesando(false);

                try {

                    Datos datos = get();

                    if (!bibliotecario
                            && datos.estudianteSesion() == null) {

                        JOptionPane.showMessageDialog(
                                vista,
                                """
                                Tu usuario no está vinculado a un
                                estudiante registrado en la biblioteca.
                                Contacta al bibliotecario.
                                """,
                                "Aviso",
                                JOptionPane.WARNING_MESSAGE
                        );

                        vista.dispose();
                        return;
                    }

                    prestamosActuales = datos.prestamos();

                    vista.mostrarDatos(
                            datos.estudiantes(),
                            datos.libros(),
                            datos.prestamos(),
                            datos.estudianteSesion()
                    );

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    vista.mostrarEstado("Consulta interrumpida.");

                } catch (ExecutionException ex) {

                    mostrarError(
                            "No se pudieron consultar los préstamos."
                    );

                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void registrarPrestamo() {

        int idEstudiante = vista.getIdEstudianteSeleccionado();
        int idLibro = vista.getIdLibroSeleccionado();

        if (idEstudiante <= 0 || idLibro <= 0) {

            mostrarError(
                    "Seleccione un estudiante y un libro."
            );
            return;
        }

        if (vista.getStockLibroSeleccionado() <= 0) {

            mostrarError(
                    "El libro seleccionado no tiene stock disponible."
            );
            return;
        }

        ejecutarOperacion(
                () -> prestamoDAO.registrarPrestamo(
                        idEstudiante, idLibro
                ),
                "Préstamo registrado correctamente."
        );
    }

    private void devolverPrestamo() {

        Integer id = vista.getIdPrestamoSeleccionado();

        if (id == null) {

            mostrarError(
                    "Seleccione un préstamo de la tabla."
            );
            return;
        }

        Prestamo seleccionado = null;

        for (Prestamo prestamo : prestamosActuales) {

            if (prestamo.getId() == id) {
                seleccionado = prestamo;
                break;
            }
        }

        if (seleccionado == null) {
            mostrarError("Préstamo no encontrado.");
            return;
        }

        if (seleccionado.isDevuelto()) {
            mostrarError("Este préstamo ya fue devuelto.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                vista,
                "¿Desea registrar la devolución del préstamo "
                        + id + "?",
                "Confirmar devolución",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutarOperacion(
                () -> prestamoDAO.registrarDevolucion(id),
                "Devolución registrada correctamente."
        );
    }

    private void ejecutarOperacion(
            Callable<Boolean> operacion,
            String mensajeExito) {

        vista.setProcesando(true);

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {
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

                        cargarDatos();

                    } else {

                        mostrarError(
                                "No fue posible completar la operación. "
                                        + "Verifique la disponibilidad "
                                        + "y el estado del préstamo."
                        );
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    vista.mostrarEstado("Operación interrumpida.");

                } catch (ExecutionException ex) {

                    Throwable causa = ex.getCause();

                    if (causa instanceof SQLException) {
                        mostrarError(
                                "Error de base de datos al procesar "
                                        + "la operación."
                        );
                    } else {
                        mostrarError(
                                "No se pudo completar la operación."
                        );
                    }

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
