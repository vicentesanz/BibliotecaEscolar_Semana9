
package dao;

import conexion.DatabaseConnection;
import modelo.Prestamo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private static final int DIAS_PRESTAMO = 7;

    public List<Prestamo> listar() throws SQLException {

        List<Prestamo> prestamos = new ArrayList<>();

        String sql = "SELECT * FROM prestamos ORDER BY id DESC";

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestamos.add(construirPrestamo(rs));
            }
        }

        return prestamos;
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante)
            throws SQLException {

        List<Prestamo> prestamos = new ArrayList<>();

        String sql = """
                SELECT * FROM prestamos
                WHERE id_estudiante = ?
                ORDER BY id DESC
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(construirPrestamo(rs));
                }
            }
        }

        return prestamos;
    }

    public synchronized boolean registrarPrestamo(
            int idEstudiante, int idLibro) throws SQLException {

        if (idEstudiante <= 0 || idLibro <= 0) {
            throw new IllegalArgumentException(
                    "Estudiante o libro no válido."
            );
        }

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection()) {

            conexion.setAutoCommit(false);

            try {
                String consultaStock = """
                        SELECT stock FROM libros
                        WHERE id = ? FOR UPDATE
                        """;

                int stock;

                try (PreparedStatement ps =
                             conexion.prepareStatement(consultaStock)) {

                    ps.setInt(1, idLibro);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conexion.rollback();
                            return false;
                        }

                        stock = rs.getInt("stock");
                    }
                }

                if (stock <= 0) {
                    conexion.rollback();
                    return false;
                }

                String actualizarStock = """
                        UPDATE libros
                        SET stock = stock - 1
                        WHERE id = ? AND stock > 0
                        """;

                try (PreparedStatement ps =
                             conexion.prepareStatement(actualizarStock)) {

                    ps.setInt(1, idLibro);

                    if (ps.executeUpdate() != 1) {
                        conexion.rollback();
                        return false;
                    }
                }

                String insertar = """
                        INSERT INTO prestamos
                        (id_estudiante, id_libro,
                         fecha_prestamo, fecha_devolucion, devuelto)
                        VALUES (?, ?, ?, ?, FALSE)
                        """;

                LocalDate hoy = LocalDate.now();
                LocalDate vencimiento = hoy.plusDays(DIAS_PRESTAMO);

                try (PreparedStatement ps =
                             conexion.prepareStatement(insertar)) {

                    ps.setInt(1, idEstudiante);
                    ps.setInt(2, idLibro);
                    ps.setDate(3, Date.valueOf(hoy));
                    ps.setDate(4, Date.valueOf(vencimiento));

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo registrar el préstamo."
                        );
                    }
                }

                conexion.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    ex.addSuppressed(errorRollback);
                }
                throw ex;
            }
        }
    }

    public synchronized boolean registrarDevolucion(int idPrestamo)
            throws SQLException {

        if (idPrestamo <= 0) {
            throw new IllegalArgumentException(
                    "ID de préstamo no válido."
            );
        }

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection()) {

            conexion.setAutoCommit(false);

            try {
                String consulta = """
                        SELECT id_libro, devuelto
                        FROM prestamos
                        WHERE id = ? FOR UPDATE
                        """;

                int idLibro;

                try (PreparedStatement ps =
                             conexion.prepareStatement(consulta)) {

                    ps.setInt(1, idPrestamo);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next() || rs.getBoolean("devuelto")) {
                            conexion.rollback();
                            return false;
                        }

                        idLibro = rs.getInt("id_libro");
                    }
                }

                String actualizarPrestamo = """
                        UPDATE prestamos
                        SET devuelto = TRUE
                        WHERE id = ? AND devuelto = FALSE
                        """;

                try (PreparedStatement ps =
                             conexion.prepareStatement(actualizarPrestamo)) {

                    ps.setInt(1, idPrestamo);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo actualizar el préstamo."
                        );
                    }
                }

                String actualizarStock = """
                        UPDATE libros
                        SET stock = stock + 1
                        WHERE id = ?
                        """;

                try (PreparedStatement ps =
                             conexion.prepareStatement(actualizarStock)) {

                    ps.setInt(1, idLibro);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo restaurar el stock."
                        );
                    }
                }

                conexion.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    ex.addSuppressed(errorRollback);
                }
                throw ex;
            }
        }
    }

    private Prestamo construirPrestamo(ResultSet rs)
            throws SQLException {

        Date fechaPrestamo = rs.getDate("fecha_prestamo");
        Date fechaDevolucion = rs.getDate("fecha_devolucion");

        return new Prestamo(
                rs.getInt("id"),
                rs.getInt("id_estudiante"),
                rs.getInt("id_libro"),
                fechaPrestamo == null
                        ? null : fechaPrestamo.toLocalDate(),
                fechaDevolucion == null
                        ? null : fechaDevolucion.toLocalDate(),
                rs.getBoolean("devuelto")
        );
    }
}
