
package dao;

import conexion.DatabaseConnection;
import modelo.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listar() throws SQLException {

        List<Categoria> categorias = new ArrayList<>();

        String sql = "SELECT * FROM categorias ORDER BY id";

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorias.add(construirCategoria(rs));
            }
        }

        return categorias;
    }

    public Categoria buscarPorId(int id) throws SQLException {

        String sql = "SELECT * FROM categorias WHERE id = ?";

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirCategoria(rs);
                }
            }
        }

        return null;
    }

    public int crear(Categoria categoria) throws SQLException {

        String sql = """
                INSERT INTO categorias (nombre)
                VALUES (?)
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNombre());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    categoria.setId(id);
                    return id;
                }
            }

            throw new SQLException(
                    "No se pudo obtener el ID de la categoria.");
        }
    }

    public boolean actualizar(Categoria categoria)
            throws SQLException {

        String sql = """
                UPDATE categorias
                SET nombre = ?
                WHERE id = ?
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM categorias WHERE id = ?";

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    private Categoria construirCategoria(ResultSet rs)
            throws SQLException {

        return new Categoria(
                rs.getInt("id"),
                rs.getString("nombre")
        );
    }
}
