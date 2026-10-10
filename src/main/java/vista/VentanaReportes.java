
package vista;

import modelo.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class VentanaReportes extends JDialog {

    private final boolean bibliotecario;

    private final JComboBox<Estudiante> comboEstudiantes =
            new JComboBox<>();

    private final JButton botonMasPrestados =
            new JButton("Libros más prestados");

    private final JButton botonHistorial =
            new JButton("Historial por estudiante");

    private final JButton botonActuales =
            new JButton("Préstamos actuales");

    private final JButton botonVolver =
            new JButton("Volver");

    private final JLabel tituloReporte =
            new JLabel("Seleccione un reporte");

    private final JLabel estado = new JLabel(" ");

    private final JTable tabla = new JTable();

    public VentanaReportes(JFrame padre, boolean bibliotecario) {

        super(padre, "Biblioteca Escolar - Reportes", true);

        this.bibliotecario = bibliotecario;

        setSize(1000, 560);
        setMinimumSize(new Dimension(800, 450));
        setLocationRelativeTo(padre);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel superior = new JPanel(new BorderLayout(10, 12));

        superior.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 5, 15)
        );

        JLabel encabezado = new JLabel(
                "Reportes de la biblioteca"
        );

        encabezado.setFont(
                new Font("SansSerif", Font.BOLD, 20)
        );

        superior.add(encabezado, BorderLayout.NORTH);

        JPanel filtros = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        filtros.add(new JLabel("Estudiante:"));

        comboEstudiantes.setPreferredSize(
                new Dimension(290, 28)
        );

        comboEstudiantes.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> lista,
                            Object valor,
                            int indice,
                            boolean seleccionado,
                            boolean foco) {

                        super.getListCellRendererComponent(
                                lista,
                                valor,
                                indice,
                                seleccionado,
                                foco
                        );

                        if (valor instanceof Estudiante e) {
                            setText(
                                    e.getNombre() + " - " + e.getRut()
                            );
                        }

                        return this;
                    }
                }
        );

        filtros.add(comboEstudiantes);
        filtros.add(botonHistorial);

        if (bibliotecario) {
            filtros.add(botonMasPrestados);
            filtros.add(botonActuales);
        }

        superior.add(filtros, BorderLayout.CENTER);

        add(superior, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(8, 8));

        centro.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        tituloReporte.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );

        centro.add(tituloReporte, BorderLayout.NORTH);

        tabla.setFillsViewportHeight(true);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        centro.add(
                new JScrollPane(tabla),
                BorderLayout.CENTER
        );

        add(centro, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout());

        inferior.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 15, 15)
        );

        inferior.add(estado, BorderLayout.CENTER);
        inferior.add(botonVolver, BorderLayout.EAST);

        add(inferior, BorderLayout.SOUTH);

        botonVolver.addActionListener(e -> dispose());
    }

    public void mostrarEstudiantes(
            List<Estudiante> estudiantes,
            Integer idEstudianteSesion) {

        comboEstudiantes.removeAllItems();

        for (Estudiante estudiante : estudiantes) {

            if (bibliotecario
                    || (idEstudianteSesion != null
                    && estudiante.getId() == idEstudianteSesion)) {

                comboEstudiantes.addItem(estudiante);
            }
        }

        comboEstudiantes.setEnabled(bibliotecario);
    }

    public int getIdEstudianteSeleccionado() {

        Estudiante estudiante =
                (Estudiante) comboEstudiantes.getSelectedItem();

        return estudiante == null ? -1 : estudiante.getId();
    }

    public void mostrarTabla(
            String titulo,
            String[] columnas,
            List<Object[]> filas) {

        DefaultTableModel modelo = new DefaultTableModel(
                columnas, 0
        ) {

            @Override
            public boolean isCellEditable(
                    int fila, int columna) {
                return false;
            }
        };

        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }

        tabla.setModel(modelo);

        tituloReporte.setText(titulo);

        estado.setText(
                "Registros encontrados: " + filas.size()
        );
    }

    public void setAccionMasPrestados(ActionListener accion) {
        botonMasPrestados.addActionListener(accion);
    }

    public void setAccionHistorial(ActionListener accion) {
        botonHistorial.addActionListener(accion);
    }

    public void setAccionActuales(ActionListener accion) {
        botonActuales.addActionListener(accion);
    }

    public void setProcesando(boolean procesando) {

        botonMasPrestados.setEnabled(!procesando);
        botonHistorial.setEnabled(!procesando);
        botonActuales.setEnabled(!procesando);

        comboEstudiantes.setEnabled(
                !procesando && bibliotecario
        );

        if (procesando) {
            estado.setText("Consultando MySQL...");
        }
    }

    public void mostrarEstado(String mensaje) {
        estado.setText(mensaje);
    }
}
