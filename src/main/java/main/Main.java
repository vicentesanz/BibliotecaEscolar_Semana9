
package main;

import dao.UsuarioDAO;
import modelo.Usuario;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        System.out.println("=== Biblioteca Escolar - Semana 9 ===");
        System.out.println("Inicio de sesion");
        System.out.println();

        System.out.print("Ingrese su RUT: ");
        String rut = scanner.nextLine().trim();

        System.out.print("Ingrese su contrasena: ");
        String contrasena = scanner.nextLine();

        try {
            Usuario usuario = usuarioDAO.autenticar(
                    rut, contrasena
            );

            if (usuario == null) {
                System.out.println(
                        "RUT o contrasena incorrectos."
                );
                return;
            }

            System.out.println();
            System.out.println(
                    "Bienvenido: " + usuario.getNombre()
            );

            System.out.println(
                    "Rol: " + usuario.getRol()
            );

            if ("bibliotecario".equals(usuario.getRol())) {
                System.out.println(
                        "Acceso administrativo identificado."
                );
            } else if ("estudiante".equals(usuario.getRol())) {
                System.out.println(
                        "Acceso de estudiante identificado."
                );
            } else {
                System.out.println(
                        "Rol no reconocido."
                );
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al autenticar: " + e.getMessage()
            );
        }
    }
}
