
package vista;

import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VentanaPrestamos extends JDialog {

    private final boolean bibliotecario;

    private final JComboBox<Estudiante> comboEstudiantes =
            new JComboBox<>();

    private final JComboBox<Libro> comboLibros =
            new JComboBox<>();

    private final JButton botonRegistrar =
            new JButton("Registrar préstamo");

    private final JButton botonDevolver =
            new JButton("Registrar devolución");

    private final JButton botonActualizar =
            new JButton("Actualizar");

    private final JButton botonCerrar =
            new JButton("Volver");

    private final JLabel estado = new JLabel("Cargando...");

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(
                    new String[]{
                            "ID", "Estudiante", "Libro",
                            "Fecha préstamo", "Vencimiento", "Estado"
                    }, 0) {

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modeloTabla);

    public VentanaPrestamos(JFrame padre, boolean bibliotecario) {

        super(padre, "Préstamos y devoluciones", true);

        this.bibliotecario = bibliotecario;

        setSize(1050, 570);
        setLocationRelativeTo(padre);
        setMinimumSize(new Dimension(850, 450));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel superior = new JPanel(new BorderLayout(8, 8));

        superior.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 5, 15)
        );

        JLabel titulo = new JLabel(
                "Gestión de préstamos y devoluciones"
        );

        titulo.setFont(
                new Font("SansSerif", Font.BOLD, 19)
        );

        superior.add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridLayout(2, 2, 10, 8));

        formulario.add(new JLabel("Estudiante:"));
        formulario.add(new JLabel("Libro disponible:"));

        formulario.add(comboEstudiantes);
        formulario.add(comboLibros);

        superior.add(formulario, BorderLayout.CENTER);

        add(superior, BorderLayout.NORTH);

        comboEstudiantes.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> lista, Object valor, int indice,
                            boolean seleccionado, boolean foco) {

                        super.getListCellRendererComponent(
                                lista, valor, indice, seleccionado, foco
                        );

                        if (valor instanceof Estudiante e) {
                            setText(e.getNombre() + " - " + e.getRut());
                        }

                        return this;
                    }
                }
        );

        comboLibros.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> lista, Object valor, int indice,
                            boolean seleccionado, boolean foco) {

                        super.getListCellRendererComponent(
                                lista, valor, indice, seleccionado, foco
                        );

                        if (valor instanceof Libro libro) {
                            setText(
                                    libro.getTitulo()
                                            + " | Stock: "
                                            + libro.getStock()
                            );
                        }

                        return this;
                    }
                }
        );

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);

        JPanel centro = new JPanel(new BorderLayout());

        centro.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(8, 8));

        inferior.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 15, 15)
        );

        JPanel botones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        botones.add(botonRegistrar);
        botones.add(botonDevolver);
        botones.add(botonActualizar);
        botones.add(botonCerrar);

        inferior.add(estado, BorderLayout.NORTH);
        inferior.add(botones, BorderLayout.SOUTH);

        add(inferior, BorderLayout.SOUTH);

        botonCerrar.addActionListener(e -> dispose());
    }

    public void mostrarDatos(
            List<Estudiante> estudiantes,
            List<Libro> libros,
            List<Prestamo> prestamos,
            Estudiante estudianteSesion) {

        comboEstudiantes.removeAllItems();
        comboLibros.removeAllItems();
        modeloTabla.setRowCount(0);

        Map<Integer, String> nombres = new HashMap<>();
        Map<Integer, String> titulos = new HashMap<>();

        for (Estudiante estudiante : estudiantes) {

            nombres.put(
                    estudiante.getId(), estudiante.getNombre()
            );

            if (bibliotecario
                    || (estudianteSesion != null
                    && estudiante.getId() == estudianteSesion.getId())) {

                comboEstudiantes.addItem(estudiante);
            }
        }

        for (Libro libro : libros) {
            titulos.put(libro.getId(), libro.getTitulo());
            comboLibros.addItem(libro);
        }

        for (Prestamo prestamo : prestamos) {

            String estadoPrestamo;

            if (prestamo.isDevuelto()) {
                estadoPrestamo = "Devuelto";
            } else if (prestamo.estaAtrasado()) {
                estadoPrestamo = "Atrasado";
            } else {
                estadoPrestamo = "En préstamo";
            }

            modeloTabla.addRow(new Object[]{
                    prestamo.getId(),
                    nombres.getOrDefault(
                            prestamo.getIdEstudiante(), "Sin nombre"
                    ),
                    titulos.getOrDefault(
                            prestamo.getIdLibro(), "Sin título"
                    ),
                    prestamo.getFechaPrestamo(),
                    prestamo.getFechaDevolucion(),
                    estadoPrestamo
            });
        }

        estado.setText(
                "Préstamos consultados: " + prestamos.size()
        );
    }

    public int getIdEstudianteSeleccionado() {
        Estudiante estudiante =
                (Estudiante) comboEstudiantes.getSelectedItem();

        return estudiante == null ? -1 : estudiante.getId();
    }

    public int getIdLibroSeleccionado() {
        Libro libro = (Libro) comboLibros.getSelectedItem();

        return libro == null ? -1 : libro.getId();
    }

    public int getStockLibroSeleccionado() {
        Libro libro = (Libro) comboLibros.getSelectedItem();

        return libro == null ? 0 : libro.getStock();
    }

    public Integer getIdPrestamoSeleccionado() {

        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            return null;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);

        return (Integer) modeloTabla.getValueAt(filaModelo, 0);
    }

    public void setAccionRegistrar(ActionListener accion) {
        botonRegistrar.addActionListener(accion);
    }

    public void setAccionDevolver(ActionListener accion) {
        botonDevolver.addActionListener(accion);
    }

    public void setAccionActualizar(ActionListener accion) {
        botonActualizar.addActionListener(accion);
    }

    public void setProcesando(boolean procesando) {

        botonRegistrar.setEnabled(!procesando);
        botonDevolver.setEnabled(!procesando);
        botonActualizar.setEnabled(!procesando);

        comboLibros.setEnabled(!procesando);

        comboEstudiantes.setEnabled(
                !procesando && bibliotecario
        );

        if (procesando) {
            estado.setText("Procesando operación...");
        }
    }

    public void mostrarEstado(String mensaje) {
        estado.setText(mensaje);
    }
}
