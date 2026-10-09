
package vista;

import modelo.Categoria;
import modelo.Libro;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FormularioLibro {

    public static Libro solicitarDatos(
            JFrame padre,
            Libro existente,
            List<Categoria> categorias) {

        if (categorias.isEmpty()) {
            JOptionPane.showMessageDialog(
                    padre,
                    "No existen categorías registradas.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        JTextField titulo = new JTextField(
                existente == null ? "" : existente.getTitulo(), 22
        );

        JTextField autor = new JTextField(
                existente == null ? "" : existente.getAutor(), 22
        );

        JTextField isbn = new JTextField(
                existente == null ? "" : existente.getIsbn(), 22
        );

        JTextField editorial = new JTextField(
                existente == null ? "" : existente.getEditorial(), 22
        );

        JSpinner stock = new JSpinner(
                new SpinnerNumberModel(
                        existente == null ? 0 : Math.max(0, existente.getStock()),
                        0, 100000, 1
                )
        );

        JComboBox<Categoria> categoria =
                new JComboBox<>(categorias.toArray(new Categoria[0]));

        if (existente != null) {
            for (int i = 0; i < categoria.getItemCount(); i++) {
                Categoria actual = categoria.getItemAt(i);

                if (actual.getId() == existente.getIdCategoria()) {
                    categoria.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Título:"));
        panel.add(titulo);

        panel.add(new JLabel("Autor:"));
        panel.add(autor);

        panel.add(new JLabel("ISBN:"));
        panel.add(isbn);

        panel.add(new JLabel("Editorial:"));
        panel.add(editorial);

        panel.add(new JLabel("Stock disponible:"));
        panel.add(stock);

        panel.add(new JLabel("Categoría:"));
        panel.add(categoria);

        String encabezado = existente == null
                ? "Registrar libro"
                : "Modificar libro";

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

            String tituloTexto = titulo.getText().trim();
            String autorTexto = autor.getText().trim();
            String isbnTexto = isbn.getText().trim();
            String editorialTexto = editorial.getText().trim();

            if (tituloTexto.isBlank()
                    || autorTexto.isBlank()
                    || isbnTexto.isBlank()
                    || editorialTexto.isBlank()) {

                JOptionPane.showMessageDialog(
                        padre,
                        "Todos los campos son obligatorios."
                );
                continue;
            }

            if (tituloTexto.length() > 200
                    || autorTexto.length() > 100
                    || isbnTexto.length() > 20
                    || editorialTexto.length() > 100) {

                JOptionPane.showMessageDialog(
                        padre,
                        "Uno de los campos supera el largo permitido."
                );
                continue;
            }

            Categoria seleccionada =
                    (Categoria) categoria.getSelectedItem();

            if (seleccionada == null) {
                JOptionPane.showMessageDialog(
                        padre,
                        "Seleccione una categoría."
                );
                continue;
            }

            int id = existente == null ? 0 : existente.getId();

            return new Libro(
                    id,
                    tituloTexto,
                    autorTexto,
                    isbnTexto,
                    editorialTexto,
                    (Integer) stock.getValue(),
                    seleccionada.getId()
            );
        }
    }
}
