
package controlador;

import dao.EstudianteDAO;
import dao.LibroDAO;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Usuario;
import vista.VentanaPrincipal;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PrincipalControlador {

    private final VentanaPrincipal vista;
    private final Usuario usuario;

    private final LibroDAO libroDAO = new LibroDAO();
    private final EstudianteDAO estudianteDAO =
            new EstudianteDAO();

    public PrincipalControlador(Usuario usuario) {
        this.usuario = usuario;
        this.vista = new VentanaPrincipal(usuario);

        vista.setAccionLibros(e -> cargarLibros());
        vista.setAccionEstudiantes(e -> cargarEstudiantes());

        vista.setAccionCerrarSesion(e -> {
            vista.dispose();
            new LoginControlador().mostrar();
        });
    }

    public void mostrar() {
        vista.setVisible(true);
        cargarLibros();

        if ("bibliotecario".equalsIgnoreCase(usuario.getRol())) {
            cargarEstudiantes();
        }
    }

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
                    List<Libro> libros = get();
                    vista.mostrarLibros(libros);
                    vista.mostrarEstado(
                            "Libros cargados: " + libros.size()
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

    private void cargarEstudiantes() {

        if (!"bibliotecario".equalsIgnoreCase(usuario.getRol())) {
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
                    List<Estudiante> estudiantes = get();
                    vista.mostrarEstudiantes(estudiantes);
                    vista.mostrarEstado(
                            "Estudiantes cargados: "
                                    + estudiantes.size()
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
}
