
package vista;

import modelo.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class VentanaCategorias extends JDialog {

    private final JButton botonRegistrar =
            new JButton("Registrar categoría");

    private final JButton botonModificar =
            new JButton("Modificar categoría");

    private final JButton botonEliminar =
            new JButton("Eliminar categoría");

    private final JButton botonActualizar =
            new JButton("Actualizar");

    private final JButton botonVolver =
            new JButton("Volver");

    private final JLabel estado = new JLabel(" ");

    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new String[]{"ID", "Nombre"}, 0
            ) {
                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modelo);

    public VentanaCategorias(JFrame padre) {

        super(padre, "Biblioteca Escolar - Categorías", true);

        setSize(750, 500);
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(padre);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel superior = new JPanel(new BorderLayout());

        superior.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 5, 15)
        );

        JLabel titulo = new JLabel(
                "Administración de categorías"
        );

        titulo.setFont(
                new Font("SansSerif", Font.BOLD, 20)
        );

        superior.add(titulo, BorderLayout.CENTER);

        add(superior, BorderLayout.NORTH);

        tabla.setFillsViewportHeight(true);
        tabla.setAutoCreateRowSorter(true);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JPanel centro = new JPanel(new BorderLayout());

        centro.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        centro.add(
                new JScrollPane(tabla),
                BorderLayout.CENTER
        );

        add(centro, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(8, 8));

        inferior.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 15, 15)
        );

        JPanel botones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        botones.add(botonRegistrar);
        botones.add(botonModificar);
        botones.add(botonEliminar);
        botones.add(botonActualizar);
        botones.add(botonVolver);

        inferior.add(estado, BorderLayout.NORTH);
        inferior.add(botones, BorderLayout.SOUTH);

        add(inferior, BorderLayout.SOUTH);

        botonVolver.addActionListener(e -> dispose());
    }

    public void mostrarCategorias(List<Categoria> categorias) {

        modelo.setRowCount(0);

        for (Categoria categoria : categorias) {

            modelo.addRow(new Object[]{
                    categoria.getId(),
                    categoria.getNombre()
            });
        }

        estado.setText(
                "Categorías registradas: " + categorias.size()
        );
    }

    public Integer getIdCategoriaSeleccionada() {

        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            return null;
        }

        int filaModelo =
                tabla.convertRowIndexToModel(fila);

        return (Integer) modelo.getValueAt(filaModelo, 0);
    }

    public String solicitarNombre(
            String encabezado,
            String nombreInicial) {

        JTextField campoNombre = new JTextField(
                nombreInicial == null ? "" : nombreInicial, 25
        );

        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.add(
                new JLabel("Nombre de la categoría:"),
                BorderLayout.NORTH
        );

        panel.add(campoNombre, BorderLayout.CENTER);

        while (true) {

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    encabezado,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (respuesta != JOptionPane.OK_OPTION) {
                return null;
            }

            String nombre = campoNombre.getText().trim();

            if (nombre.isBlank()) {

                JOptionPane.showMessageDialog(
                        this,
                        "El nombre de la categoría es obligatorio."
                );

                continue;
            }

            if (nombre.length() > 100) {

                JOptionPane.showMessageDialog(
                        this,
                        "El nombre no puede superar los 100 caracteres."
                );

                continue;
            }

            return nombre;
        }
    }

    public void setAccionRegistrar(ActionListener accion) {
        botonRegistrar.addActionListener(accion);
    }

    public void setAccionModificar(ActionListener accion) {
        botonModificar.addActionListener(accion);
    }

    public void setAccionEliminar(ActionListener accion) {
        botonEliminar.addActionListener(accion);
    }

    public void setAccionActualizar(ActionListener accion) {
        botonActualizar.addActionListener(accion);
    }

    public void setProcesando(boolean procesando) {

        botonRegistrar.setEnabled(!procesando);
        botonModificar.setEnabled(!procesando);
        botonEliminar.setEnabled(!procesando);
        botonActualizar.setEnabled(!procesando);

        if (procesando) {
            estado.setText("Procesando operación...");
        }
    }

    public void mostrarEstado(String mensaje) {
        estado.setText(mensaje);
    }
}
