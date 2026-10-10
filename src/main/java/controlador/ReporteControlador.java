
package controlador;

import dao.EstudianteDAO;
import dao.ReporteDAO;

import modelo.Estudiante;
import modelo.Usuario;
import vista.VentanaReportes;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ReporteControlador {

    private final Usuario usuario;
    private final VentanaReportes vista;
    private final boolean bibliotecario;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final EstudianteDAO estudianteDAO =
            new EstudianteDAO();

    private int idEstudianteSesion = -1;

    public ReporteControlador(JFrame padre, Usuario usuario) {

        this.usuario = usuario;

        bibliotecario =
                "bibliotecario".equalsIgnoreCase(usuario.getRol());

        vista = new VentanaReportes(padre, bibliotecario);

        vista.setAccionMasPrestados(
                e -> cargarLibrosMasPrestados()
        );

        vista.setAccionHistorial(
                e -> cargarHistorial()
        );

        vista.setAccionActuales(
                e -> cargarPrestamosActuales()
        );
    }

    public void mostrar() {

        cargarEstudiantes();
        vista.setVisible(true);
    }

    private void cargarEstudiantes() {

        vista.setProcesando(true);

        new SwingWorker<List<Estudiante>, Void>() {

            @Override
            protected List<Estudiante> doInBackground()
                    throws Exception {

                return estudianteDAO.listar();
            }

            @Override
            protected void done() {

                vista.setProcesando(false);

                try {

                    List<Estudiante> estudiantes = get();

                    if (!bibliotecario) {

                        for (Estudiante estudiante : estudiantes) {

                            if (estudiante.getRut()
                                    .equalsIgnoreCase(usuario.getRut())) {

                                idEstudianteSesion =
                                        estudiante.getId();

                                break;
                            }
                        }

                        if (idEstudianteSesion <= 0) {

                            mostrarError(
                                    "Tu cuenta no tiene un estudiante "
                                            + "asociado."
                            );

                            vista.dispose();
                            return;
                        }
                    }

                    vista.mostrarEstudiantes(
                            estudiantes,
                            bibliotecario
                                    ? null
                                    : idEstudianteSesion
                    );

                    if (bibliotecario) {
                        cargarLibrosMasPrestados();
                    } else {
                        cargarHistorial();
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    vista.mostrarEstado(
                            "Consulta interrumpida."
                    );

                } catch (ExecutionException ex) {

                    mostrarError(
                            "No se pudieron cargar los estudiantes."
                    );

                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void cargarLibrosMasPrestados() {

        if (!bibliotecario) {
            return;
        }

        consultar(
                () -> reporteDAO.librosMasPrestados(),

                libros -> {

                    List<Object[]> filas = new ArrayList<>();

                    for (ReporteDAO.LibroMasPrestado libro : libros) {

                        filas.add(new Object[]{
                                libro.idLibro(),
                                libro.titulo(),
                                libro.autor(),
                                libro.totalPrestamos()
                        });
                    }

                    vista.mostrarTabla(
                            "Libros más prestados",
                            new String[]{
                                    "ID", "Título", "Autor",
                                    "Total de préstamos"
                            },
                            filas
                    );
                },

                "No se pudo generar el reporte de libros."
        );
    }

    private void cargarHistorial() {

        int idEstudiante =
                vista.getIdEstudianteSeleccionado();

        if (idEstudiante <= 0) {

            mostrarError(
                    "Seleccione un estudiante."
            );

            return;
        }

        if (!bibliotecario
                && idEstudiante != idEstudianteSesion) {

            mostrarError(
                    "Solo puedes consultar tu propio historial."
            );

            return;
        }

        consultar(
                () -> reporteDAO.historialEstudiante(
                        idEstudiante
                ),

                prestamos -> {

                    List<Object[]> filas = new ArrayList<>();

                    for (ReporteDAO.DetallePrestamo p : prestamos) {

                        filas.add(new Object[]{
                                p.idPrestamo(),
                                p.estudiante(),
                                p.libro(),
                                p.fechaPrestamo(),
                                p.fechaVencimiento(),
                                obtenerEstado(p)
                        });
                    }

                    vista.mostrarTabla(
                            "Historial de préstamos del estudiante",
                            columnasPrestamos(),
                            filas
                    );
                },

                "No se pudo consultar el historial."
        );
    }

    private void cargarPrestamosActuales() {

        if (!bibliotecario) {
            return;
        }

        consultar(
                () -> reporteDAO.prestamosActuales(),

                prestamos -> {

                    List<Object[]> filas = new ArrayList<>();

                    for (ReporteDAO.DetallePrestamo p : prestamos) {

                        filas.add(new Object[]{
                                p.idPrestamo(),
                                p.estudiante(),
                                p.libro(),
                                p.fechaPrestamo(),
                                p.fechaVencimiento(),
                                obtenerEstado(p)
                        });
                    }

                    vista.mostrarTabla(
                            "Libros actualmente en préstamo",
                            columnasPrestamos(),
                            filas
                    );
                },

                "No se pudieron consultar los préstamos activos."
        );
    }

    private String[] columnasPrestamos() {

        return new String[]{
                "ID préstamo",
                "Estudiante",
                "Libro",
                "Fecha préstamo",
                "Vencimiento",
                "Estado"
        };
    }

    private String obtenerEstado(
            ReporteDAO.DetallePrestamo prestamo) {

        if (prestamo.devuelto()) {
            return "Devuelto";
        }

        if (prestamo.estaAtrasado()) {
            return "Atrasado";
        }

        return "En préstamo";
    }

    private <T> void consultar(
            Callable<List<T>> consulta,
            Consumer<List<T>> mostrar,
            String mensajeError) {

        vista.setProcesando(true);

        new SwingWorker<List<T>, Void>() {

            @Override
            protected List<T> doInBackground()
                    throws Exception {

                return consulta.call();
            }

            @Override
            protected void done() {

                vista.setProcesando(false);

                try {

                    List<T> resultados = get();
                    mostrar.accept(resultados);

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    vista.mostrarEstado(
                            "Consulta interrumpida."
                    );

                } catch (ExecutionException ex) {

                    mostrarError(mensajeError);
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
