
package controlador;

import dao.UsuarioDAO;
import modelo.Usuario;
import vista.VentanaLogin;

import javax.swing.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class LoginControlador {

    private final VentanaLogin vista = new VentanaLogin();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public LoginControlador() {
        vista.setAccionIngresar(e -> iniciarSesion());
    }

    public void mostrar() {
        vista.setVisible(true);
    }

    private void iniciarSesion() {

        String rut = vista.getRut();
        char[] caracteres = vista.getContrasena();

        if (rut.isBlank() || caracteres.length == 0) {
            Arrays.fill(caracteres, '\0');
            vista.mostrarError("Ingrese RUT y contraseña.");
            return;
        }

        String contrasena = new String(caracteres);
        Arrays.fill(caracteres, '\0');

        vista.setProcesando(true);

        SwingWorker<Usuario, Void> tarea = new SwingWorker<>() {

            @Override
            protected Usuario doInBackground() throws Exception {
                return usuarioDAO.autenticar(rut, contrasena);
            }

            @Override
            protected void done() {
                vista.setProcesando(false);

                try {
                    Usuario usuario = get();

                    if (usuario == null) {
                        vista.mostrarError(
                                "RUT o contraseña incorrectos."
                        );
                        return;
                    }

                    String rol = usuario.getRol();

                    if (!"bibliotecario".equalsIgnoreCase(rol)
                            && !"estudiante".equalsIgnoreCase(rol)) {
                        vista.mostrarError("Rol no autorizado.");
                        return;
                    }

                    vista.dispose();

                    new PrincipalControlador(usuario).mostrar();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarError("Operación interrumpida.");

                } catch (ExecutionException ex) {
                    vista.mostrarError(
                            "Error al conectar con MySQL."
                    );
                    ex.printStackTrace();
                }
            }
        };

        tarea.execute();
    }
}
