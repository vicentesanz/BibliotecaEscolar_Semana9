
package vista;

import modelo.Estudiante;

import javax.swing.*;
import java.awt.*;

public class FormularioEstudiante {

    public static Estudiante solicitarDatos(
            JFrame padre,
            Estudiante existente) {

        JTextField nombre = new JTextField(
                existente == null ? "" : existente.getNombre(), 22
        );

        JTextField rut = new JTextField(
                existente == null ? "" : existente.getRut(), 22
        );

        JTextField curso = new JTextField(
                existente == null ? "" : existente.getCurso(), 22
        );

        JTextField correo = new JTextField(
                existente == null ? "" : existente.getCorreo(), 22
        );

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Nombre:"));
        panel.add(nombre);

        panel.add(new JLabel("RUT:"));
        panel.add(rut);

        panel.add(new JLabel("Curso:"));
        panel.add(curso);

        panel.add(new JLabel("Correo:"));
        panel.add(correo);

        String encabezado = existente == null
                ? "Registrar estudiante"
                : "Modificar estudiante";

        while (true) {

            int respuesta = JOptionPane.showConfirmDialog(
                    padre,
                    panel,
                    encabezado,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (respuesta != JOptionPane.OK_OPTION) {
                return null;
            }

            String nombreTexto = nombre.getText().trim();
            String rutTexto = rut.getText().trim();
            String cursoTexto = curso.getText().trim();
            String correoTexto = correo.getText().trim();

            if (nombreTexto.isBlank()
                    || rutTexto.isBlank()
                    || cursoTexto.isBlank()
                    || correoTexto.isBlank()) {

                JOptionPane.showMessageDialog(
                        padre,
                        "Todos los campos son obligatorios."
                );
                continue;
            }

            if (nombreTexto.length() > 100
                    || rutTexto.length() > 12
                    || cursoTexto.length() > 20
                    || correoTexto.length() > 100) {

                JOptionPane.showMessageDialog(
                        padre,
                        "Uno de los campos supera el largo permitido."
                );
                continue;
            }

            if (!rutTexto.matches("\\d{7,8}-[0-9kK]")) {

                JOptionPane.showMessageDialog(
                        padre,
                        "Ingrese el RUT con formato 12345678-9."
                );
                continue;
            }

            if (!correoTexto.matches(
                    "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

                JOptionPane.showMessageDialog(
                        padre,
                        "Ingrese un correo electrónico válido."
                );
                continue;
            }

            int id = existente == null ? 0 : existente.getId();

            return new Estudiante(
                    id,
                    nombreTexto,
                    rutTexto,
                    correoTexto,
                    cursoTexto
            );
        }
    }
}
