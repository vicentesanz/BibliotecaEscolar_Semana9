
package dao;

import conexion.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    public record LibroMasPrestado(
            int idLibro,
            String titulo,
            String autor,
            long totalPrestamos) {
    }

    public record DetallePrestamo(
            int idPrestamo,
            String estudiante,
            String libro,
            LocalDate fechaPrestamo,
            LocalDate fechaVencimiento,
            boolean devuelto) {

        public boolean estaAtrasado() {
            return !devuelto
                    && fechaVencimiento != null
                    && fechaVencimiento.isBefore(LocalDate.now());
        }
    }

    // REPORTE 1: LIBROS MAS PRESTADOS

    public List<LibroMasPrestado> librosMasPrestados()
            throws SQLException {

        List<LibroMasPrestado> resultado = new ArrayList<>();

        String sql = """
                SELECT l.id AS id_libro,
                       l.titulo,
                       l.autor,
                       COUNT(p.id) AS total_prestamos
                FROM libros l
                INNER JOIN prestamos p
                    ON l.id = p.id_libro
                GROUP BY l.id, l.titulo, l.autor
                ORDER BY total_prestamos DESC, l.titulo
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                resultado.add(new LibroMasPrestado(
                        rs.getInt("id_libro"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getLong("total_prestamos")
                ));
            }
        }

        return resultado;
    }

    // REPORTE 2: HISTORIAL DE UN ESTUDIANTE

    public List<DetallePrestamo> historialEstudiante(
            int idEstudiante) throws SQLException {

        if (idEstudiante <= 0) {
            throw new IllegalArgumentException(
                    "ID de estudiante no válido."
            );
        }

        List<DetallePrestamo> resultado = new ArrayList<>();

        String sql = """
                SELECT p.id AS id_prestamo,
                       e.nombre AS estudiante,
                       l.titulo AS libro,
                       p.fecha_prestamo,
                       p.fecha_devolucion,
                       p.devuelto
                FROM prestamos p
                INNER JOIN estudiantes e
                    ON p.id_estudiante = e.id
                INNER JOIN libros l
                    ON p.id_libro = l.id
                WHERE p.id_estudiante = ?
                ORDER BY p.fecha_prestamo DESC, p.id DESC
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    resultado.add(construirDetalle(rs));
                }
            }
        }

        return resultado;
    }

    // REPORTE 3: PRESTAMOS ACTUALMENTE ACTIVOS

    public List<DetallePrestamo> prestamosActuales()
            throws SQLException {

        List<DetallePrestamo> resultado = new ArrayList<>();

        String sql = """
                SELECT p.id AS id_prestamo,
                       e.nombre AS estudiante,
                       l.titulo AS libro,
                       p.fecha_prestamo,
                       p.fecha_devolucion,
                       p.devuelto
                FROM prestamos p
                INNER JOIN estudiantes e
                    ON p.id_estudiante = e.id
                INNER JOIN libros l
                    ON p.id_libro = l.id
                WHERE p.devuelto = FALSE
                ORDER BY p.fecha_devolucion ASC, p.id ASC
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(construirDetalle(rs));
            }
        }

        return resultado;
    }

    private DetallePrestamo construirDetalle(ResultSet rs)
            throws SQLException {

        Date fechaPrestamo = rs.getDate("fecha_prestamo");
        Date fechaDevolucion = rs.getDate("fecha_devolucion");

        return new DetallePrestamo(
                rs.getInt("id_prestamo"),
                rs.getString("estudiante"),
                rs.getString("libro"),
                fechaPrestamo == null
                        ? null : fechaPrestamo.toLocalDate(),
                fechaDevolucion == null
                        ? null : fechaDevolucion.toLocalDate(),
                rs.getBoolean("devuelto")
        );
    }
}
