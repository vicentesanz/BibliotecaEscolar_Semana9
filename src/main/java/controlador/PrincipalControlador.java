
package controlador;

import dao.CategoriaDAO;
import dao.EstudianteDAO;
import dao.LibroDAO;

import modelo.Categoria;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Usuario;

import vista.FormularioEstudiante;
import vista.FormularioLibro;
import vista.VentanaPrincipal;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class PrincipalControlador {

    private final VentanaPrincipal vista;
    private final Usuario usuario;

    private final LibroDAO libroDAO = new LibroDAO();

    private final EstudianteDAO estudianteDAO =
            new EstudianteDAO();

    private final CategoriaDAO categoriaDAO =
            new CategoriaDAO();

    private List<Libro> librosActuales = List.of();

    private List<Estudiante> estudiantesActuales = List.of();

    public PrincipalControlador(Usuario usuario) {

        this.usuario = usuario;
        this.vista = new VentanaPrincipal(usuario);

        vista.setAccionLibros(e -> cargarLibros());
        vista.setAccionEstudiantes(e -> cargarEstudiantes());

        vista.setAccionRegistrarLibro(e -> registrarLibro());
        vista.setAccionModificarLibro(e -> modificarLibro());
        vista.setAccionEliminarLibro(e -> eliminarLibro());

        vista.setAccionRegistrarEstudiante(
                e -> registrarEstudiante()
        );

        vista.setAccionModificarEstudiante(
                e -> modificarEstudiante()
        );

        vista.setAccionEliminarEstudiante(
                e -> eliminarEstudiante()
        );

        // Semana 9: acceso al modulo de prestamos
        vista.setAccionPrestamos(e -> abrirPrestamos());

        vista.setAccionCerrarSesion(e -> {
            vista.dispose();
            new LoginControlador().mostrar();
        });
    }

    private boolean esBibliotecario() {
        return "bibliotecario".equalsIgnoreCase(
                usuario.getRol()
        );
    }

    public void mostrar() {
        vista.setVisible(true);
        cargarLibros();

        if (esBibliotecario()) {
            cargarEstudiantes();
        }
    }

    // MODULO DE PRESTAMOS

    private void abrirPrestamos() {

        new PrestamoControlador(
                vista, usuario
        ).mostrar();

        // Actualizar el inventario al volver
        cargarLibros();
    }

    // GESTION DE LIBROS

    private void cargarLibros() {

        vista.setCargandoLibros(true);

        new SwingWorker<List<Libro>, Void>() {

            @Override
            protected List<Libro> doInBackground()
                    throws Exception {
                return libroDAO.listar();
            }

            @Override
            protected void done() {

                vista.setCargandoLibros(false);

                try {
                    librosActuales = get();

                    vista.mostrarLibros(librosActuales);

                    vista.mostrarEstado(
                            "Libros cargados: "
                                    + librosActuales.size()
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarEstado("Consulta interrumpida.");

                } catch (ExecutionException ex) {
                    vista.mostrarEstado(
                            "Error al consultar los libros."
                    );
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private Libro obtenerLibroSeleccionado() {

        Integer id = vista.getIdLibroSeleccionado();

        if (id == null) {
            JOptionPane.showMessageDialog(
                    vista,
                    "Seleccione primero un libro de la tabla."
            );
            return null;
        }

        for (Libro libro : librosActuales) {
            if (libro.getId() == id) {
                return libro;
            }
        }

        JOptionPane.showMessageDialog(
                vista,
                "No se encontró el libro seleccionado."
        );

        return null;
    }

    private void registrarLibro() {

        if (!esBibliotecario()) {
            return;
        }

        abrirFormularioLibro(null);
    }

    private void modificarLibro() {

        if (!esBibliotecario()) {
            return;
        }

        Libro libro = obtenerLibroSeleccionado();

        if (libro != null) {
            abrirFormularioLibro(libro);
        }
    }

    private void abrirFormularioLibro(Libro original) {

        vista.setCargandoLibros(true);
        vista.mostrarEstado("Cargando categorías...");

        new SwingWorker<List<Categoria>, Void>() {

            @Override
            protected List<Categoria> doInBackground()
                    throws Exception {
                return categoriaDAO.listar();
            }

            @Override
            protected void done() {

                vista.setCargandoLibros(false);

                try {
                    List<Categoria> categorias = get();

                    Libro datos = FormularioLibro.solicitarDatos(
                            vista, original, categorias
                    );

                    if (datos == null) {
                        vista.mostrarEstado("Operación cancelada.");
                        return;
                    }

                    if (original == null) {

                        ejecutarCambioLibro(
                                () -> libroDAO.crear(datos) > 0,
                                "Libro registrado correctamente."
                        );

                    } else {

                        ejecutarCambioLibro(
                                () -> libroDAO.actualizar(datos),
                                "Libro modificado correctamente."
                        );
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarEstado("Operación interrumpida.");

                } catch (ExecutionException ex) {
                    vista.mostrarEstado(
                            "No se pudieron cargar las categorías."
                    );
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void eliminarLibro() {

        if (!esBibliotecario()) {
            return;
        }

        Libro libro = obtenerLibroSeleccionado();

        if (libro == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                vista,
                "¿Desea eliminar el libro \""
                        + libro.getTitulo() + "\"?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutarCambioLibro(
                () -> libroDAO.eliminar(libro.getId()),
                "Libro eliminado correctamente."
        );
    }

    private void ejecutarCambioLibro(
            Callable<Boolean> operacion,
            String mensajeExito) {

        vista.setCargandoLibros(true);
        vista.mostrarEstado("Procesando operación...");

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {

                vista.setCargandoLibros(false);

                try {
                    boolean correcto = get();

                    if (correcto) {
                        JOptionPane.showMessageDialog(
                                vista, mensajeExito
                        );
                        cargarLibros();

                    } else {
                        JOptionPane.showMessageDialog(
                                vista,
                                "No se encontró el registro.",
                                "Aviso",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarEstado("Operación interrumpida.");

                } catch (ExecutionException ex) {

                    Throwable causa = ex.getCause();

                    String mensaje =
                            "No se pudo completar la operación.";

                    if (causa instanceof SQLException sqlEx
                            && "23000".equals(sqlEx.getSQLState())) {

                        mensaje = """
                                No se pudo guardar o eliminar el libro.
                                Compruebe que el ISBN no esté repetido
                                y que el libro no tenga préstamos asociados.
                                """;
                    }

                    JOptionPane.showMessageDialog(
                            vista, mensaje,
                            "Error", JOptionPane.ERROR_MESSAGE
                    );

                    vista.mostrarEstado(
                            "La operación no se completó."
                    );
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // GESTION DE ESTUDIANTES

    private void cargarEstudiantes() {

        if (!esBibliotecario()) {
            return;
        }

        vista.setCargandoEstudiantes(true);

        new SwingWorker<List<Estudiante>, Void>() {

            @Override
            protected List<Estudiante> doInBackground()
                    throws Exception {
                return estudianteDAO.listar();
            }

            @Override
            protected void done() {

                vista.setCargandoEstudiantes(false);

                try {
                    estudiantesActuales = get();

                    vista.mostrarEstudiantes(estudiantesActuales);

                    vista.mostrarEstado(
                            "Estudiantes cargados: "
                                    + estudiantesActuales.size()
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarEstado("Consulta interrumpida.");

                } catch (ExecutionException ex) {
                    vista.mostrarEstado(
                            "Error al consultar estudiantes."
                    );
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private Estudiante obtenerEstudianteSeleccionado() {

        Integer id = vista.getIdEstudianteSeleccionado();

        if (id == null) {
            JOptionPane.showMessageDialog(
                    vista, "Seleccione primero un estudiante."
            );
            return null;
        }

        for (Estudiante estudiante : estudiantesActuales) {
            if (estudiante.getId() == id) {
                return estudiante;
            }
        }

        JOptionPane.showMessageDialog(
                vista,
                "No se encontró el estudiante seleccionado."
        );

        return null;
    }

    private void registrarEstudiante() {

        if (!esBibliotecario()) {
            return;
        }

        Estudiante datos = FormularioEstudiante.solicitarDatos(
                vista, null
        );

        if (datos == null) {
            return;
        }

        ejecutarCambioEstudiante(
                () -> estudianteDAO.crear(datos) > 0,
                "Estudiante registrado correctamente."
        );
    }

    private void modificarEstudiante() {

        if (!esBibliotecario()) {
            return;
        }

        Estudiante original = obtenerEstudianteSeleccionado();

        if (original == null) {
            return;
        }

        Estudiante datos = FormularioEstudiante.solicitarDatos(
                vista, original
        );

        if (datos == null) {
            return;
        }

        ejecutarCambioEstudiante(
                () -> estudianteDAO.actualizar(datos),
                "Estudiante modificado correctamente."
        );
    }

    private void eliminarEstudiante() {

        if (!esBibliotecario()) {
            return;
        }

        Estudiante estudiante = obtenerEstudianteSeleccionado();

        if (estudiante == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                vista,
                "¿Desea eliminar al estudiante \""
                        + estudiante.getNombre() + "\"?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutarCambioEstudiante(
                () -> estudianteDAO.eliminar(
                        estudiante.getId()
                ),
                "Estudiante eliminado correctamente."
        );
    }

    private void ejecutarCambioEstudiante(
            Callable<Boolean> operacion,
            String mensajeExito) {

        vista.setCargandoEstudiantes(true);

        vista.mostrarEstado(
                "Procesando operación de estudiante..."
        );

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {
                return operacion.call();
            }

            @Override
            protected void done() {

                vista.setCargandoEstudiantes(false);

                try {
                    boolean correcto = get();

                    if (correcto) {
                        JOptionPane.showMessageDialog(
                                vista, mensajeExito
                        );
                        cargarEstudiantes();

                    } else {
                        JOptionPane.showMessageDialog(
                                vista,
                                "No se encontró el estudiante.",
                                "Aviso",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarEstado("Operación interrumpida.");

                } catch (ExecutionException ex) {

                    Throwable causa = ex.getCause();

                    String mensaje =
                            "No se pudo completar la operación.";

                    if (causa instanceof SQLException sqlEx) {

                        if (sqlEx.getErrorCode() == 1062) {
                            mensaje =
                                    "Ya existe un estudiante con ese RUT.";

                        } else if (sqlEx.getErrorCode() == 1451) {
                            mensaje = """
                                    No se puede eliminar el estudiante
                                    porque tiene préstamos asociados.
                                    """;

                        } else if (
                                "23000".equals(sqlEx.getSQLState())) {

                            mensaje = """
                                    La operación incumple una restricción
                                    de la base de datos.
                                    """;
                        }
                    }

                    JOptionPane.showMessageDialog(
                            vista, mensaje,
                            "Error", JOptionPane.ERROR_MESSAGE
                    );

                    vista.mostrarEstado(
                            "La operación no se completó."
                    );
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}
