
package vista;

import modelo.Estudiante;
import modelo.Libro;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private final JButton botonCerrarSesion =
            new JButton("Cerrar sesión");

    private final JButton botonLibros =
            new JButton("Actualizar libros");

    private final JButton botonEstudiantes =
            new JButton("Actualizar estudiantes");

    private final JButton botonRegistrarLibro =
            new JButton("Registrar libro");

    private final JButton botonModificarLibro =
            new JButton("Modificar libro");

    private final JButton botonEliminarLibro =
            new JButton("Eliminar libro");

    private final JButton botonRegistrarEstudiante =
            new JButton("Registrar estudiante");

    private final JButton botonModificarEstudiante =
            new JButton("Modificar estudiante");

    private final JButton botonEliminarEstudiante =
            new JButton("Eliminar estudiante");

    private final DefaultTableModel modeloLibros =
            crearModelo("ID", "Título", "Autor", "Stock", "Categoría");

    private final DefaultTableModel modeloEstudiantes =
            crearModelo("ID", "Nombre", "RUT", "Curso", "Correo");

    private final JTable tablaLibros = new JTable(modeloLibros);

    private final JTable tablaEstudiantes =
            new JTable(modeloEstudiantes);

    private final JLabel estado = new JLabel(" ");

    private final boolean bibliotecario;

    public VentanaPrincipal(Usuario usuario) {

        bibliotecario =
                "bibliotecario".equalsIgnoreCase(usuario.getRol());

        setTitle("Biblioteca Escolar - Panel principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 450));
        setLayout(new BorderLayout(10, 10));

        JPanel superior = new JPanel(new BorderLayout(10, 10));

        superior.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 5, 15)
        );

        JLabel bienvenida = new JLabel(
                "Bienvenido: " + usuario.getNombre()
                        + " | Rol: " + usuario.getRol()
        );

        bienvenida.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );

        superior.add(bienvenida, BorderLayout.CENTER);
        superior.add(botonCerrarSesion, BorderLayout.EAST);

        add(superior, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab("Libros", crearPanelLibros());

        if (bibliotecario) {
            pestanas.addTab(
                    "Estudiantes", crearPanelEstudiantes()
            );
        }

        add(pestanas, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout());

        inferior.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 10, 15)
        );

        inferior.add(estado, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelLibros() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        panel.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        tablaLibros.setFillsViewportHeight(true);
        tablaLibros.setAutoCreateRowSorter(true);

        tablaLibros.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        panel.add(
                new JScrollPane(tablaLibros),
                BorderLayout.CENTER
        );

        JPanel botones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        if (bibliotecario) {
            botones.add(botonRegistrarLibro);
            botones.add(botonModificarLibro);
            botones.add(botonEliminarLibro);
        }

        botones.add(botonLibros);

        panel.add(botones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelEstudiantes() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        panel.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        tablaEstudiantes.setFillsViewportHeight(true);
        tablaEstudiantes.setAutoCreateRowSorter(true);

        tablaEstudiantes.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        panel.add(
                new JScrollPane(tablaEstudiantes),
                BorderLayout.CENTER
        );

        JPanel botones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        botones.add(botonRegistrarEstudiante);
        botones.add(botonModificarEstudiante);
        botones.add(botonEliminarEstudiante);
        botones.add(botonEstudiantes);

        panel.add(botones, BorderLayout.SOUTH);

        return panel;
    }

    private DefaultTableModel crearModelo(String... columnas) {

        return new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public void mostrarLibros(List<Libro> libros) {

        modeloLibros.setRowCount(0);

        for (Libro libro : libros) {

            modeloLibros.addRow(new Object[]{
                    libro.getId(),
                    libro.getTitulo(),
                    libro.getAutor(),
                    libro.getStock(),
                    libro.getIdCategoria()
            });
        }
    }

    public void mostrarEstudiantes(List<Estudiante> estudiantes) {

        modeloEstudiantes.setRowCount(0);

        for (Estudiante estudiante : estudiantes) {

            modeloEstudiantes.addRow(new Object[]{
                    estudiante.getId(),
                    estudiante.getNombre(),
                    estudiante.getRut(),
                    estudiante.getCurso(),
                    estudiante.getCorreo()
            });
        }
    }

    public Integer getIdLibroSeleccionado() {

        int fila = tablaLibros.getSelectedRow();

        if (fila < 0) {
            return null;
        }

        int filaModelo =
                tablaLibros.convertRowIndexToModel(fila);

        return (Integer) modeloLibros.getValueAt(
                filaModelo, 0
        );
    }

    public Integer getIdEstudianteSeleccionado() {

        int fila = tablaEstudiantes.getSelectedRow();

        if (fila < 0) {
            return null;
        }

        int filaModelo =
                tablaEstudiantes.convertRowIndexToModel(fila);

        return (Integer) modeloEstudiantes.getValueAt(
                filaModelo, 0
        );
    }

    public void setAccionLibros(ActionListener accion) {
        botonLibros.addActionListener(accion);
    }

    public void setAccionRegistrarLibro(ActionListener accion) {
        botonRegistrarLibro.addActionListener(accion);
    }

    public void setAccionModificarLibro(ActionListener accion) {
        botonModificarLibro.addActionListener(accion);
    }

    public void setAccionEliminarLibro(ActionListener accion) {
        botonEliminarLibro.addActionListener(accion);
    }

    public void setAccionEstudiantes(ActionListener accion) {
        botonEstudiantes.addActionListener(accion);
    }

    public void setAccionRegistrarEstudiante(
            ActionListener accion) {
        botonRegistrarEstudiante.addActionListener(accion);
    }

    public void setAccionModificarEstudiante(
            ActionListener accion) {
        botonModificarEstudiante.addActionListener(accion);
    }

    public void setAccionEliminarEstudiante(
            ActionListener accion) {
        botonEliminarEstudiante.addActionListener(accion);
    }

    public void setAccionCerrarSesion(ActionListener accion) {
        botonCerrarSesion.addActionListener(accion);
    }

    public void setCargandoLibros(boolean cargando) {

        botonLibros.setEnabled(!cargando);
        botonRegistrarLibro.setEnabled(!cargando);
        botonModificarLibro.setEnabled(!cargando);
        botonEliminarLibro.setEnabled(!cargando);
    }

    public void setCargandoEstudiantes(boolean cargando) {

        botonEstudiantes.setEnabled(!cargando);
        botonRegistrarEstudiante.setEnabled(!cargando);
        botonModificarEstudiante.setEnabled(!cargando);
        botonEliminarEstudiante.setEnabled(!cargando);
    }

    public void mostrarEstado(String texto) {
        estado.setText(texto);
    }
}
