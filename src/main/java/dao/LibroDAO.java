
package dao;

import conexion.DatabaseConnection;
import modelo.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    public List<Libro> listar() throws SQLException {

        List<Libro> libros = new ArrayList<>();

        String sql = "SELECT * FROM libros ORDER BY id";

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                libros.add(construirLibro(rs));
            }
        }

        return libros;
    }

    public Libro buscarPorId(int id) throws SQLException {

        String sql = "SELECT * FROM libros WHERE id = ?";

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirLibro(rs);
                }
            }
        }

        return null;
    }

    public int crear(Libro libro) throws SQLException {

        String sql = """
                INSERT INTO libros
                (titulo, autor, isbn, editorial, stock, id_categoria)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setString(4, libro.getEditorial());
            ps.setInt(5, libro.getStock());
            ps.setInt(6, libro.getIdCategoria());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    libro.setId(id);
                    return id;
                }
            }

            throw new SQLException(
                    "No se pudo obtener el ID del libro.");
        }
    }

    public boolean actualizar(Libro libro) throws SQLException {

        String sql = """
                UPDATE libros
                SET titulo = ?, autor = ?, isbn = ?,
                    editorial = ?, stock = ?, id_categoria = ?
                WHERE id = ?
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getIsbn());
            ps.setString(4, libro.getEditorial());
            ps.setInt(5, libro.getStock());
            ps.setInt(6, libro.getIdCategoria());
            ps.setInt(7, libro.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM libros WHERE id = ?";

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Libro construirLibro(ResultSet rs) throws SQLException {

        return new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("isbn"),
                rs.getString("editorial"),
                rs.getInt("stock"),
                rs.getInt("id_categoria")
        );
    }
}
