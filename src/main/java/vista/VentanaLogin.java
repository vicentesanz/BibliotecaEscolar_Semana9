
package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class VentanaLogin extends JFrame {

    private final JTextField campoRut = new JTextField(20);
    private final JPasswordField campoContrasena =
            new JPasswordField(20);
    private final JButton botonIngresar =
            new JButton("Iniciar sesión");
    private final JLabel mensaje = new JLabel(" ");

    public VentanaLogin() {
        setTitle("Biblioteca Escolar - Inicio de sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 340);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBorder(new EmptyBorder(20, 35, 20, 35));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(7, 0, 7, 0);

        JLabel titulo = new JLabel(
                "Sistema de Biblioteca Escolar",
                SwingConstants.CENTER
        );
        titulo.setFont(new Font("SansSerif", Font.BOLD, 19));

        c.gridy = 0;
        contenido.add(titulo, c);

        c.gridy = 1;
        contenido.add(new JLabel("RUT:"), c);

        c.gridy = 2;
        contenido.add(campoRut, c);

        c.gridy = 3;
        contenido.add(new JLabel("Contraseña:"), c);

        c.gridy = 4;
        contenido.add(campoContrasena, c);

        c.gridy = 5;
        contenido.add(botonIngresar, c);

        mensaje.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 6;
        contenido.add(mensaje, c);

        add(contenido);
        getRootPane().setDefaultButton(botonIngresar);
    }

    public String getRut() {
        return campoRut.getText().trim();
    }

    public char[] getContrasena() {
        return campoContrasena.getPassword();
    }

    public void setAccionIngresar(ActionListener accion) {
        botonIngresar.addActionListener(accion);
    }

    public void setProcesando(boolean procesando) {
        botonIngresar.setEnabled(!procesando);
        campoRut.setEnabled(!procesando);
        campoContrasena.setEnabled(!procesando);

        mensaje.setText(
                procesando ? "Verificando credenciales..." : " "
        );
    }

    public void mostrarError(String error) {
        mensaje.setForeground(new Color(170, 35, 35));
        mensaje.setText(error);
    }
}
