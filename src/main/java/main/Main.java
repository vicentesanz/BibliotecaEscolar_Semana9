
package main;

import controlador.LoginControlador;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new LoginControlador().mostrar();
        });
    }
}
