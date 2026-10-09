
package dao;

import conexion.DatabaseConnection;
import modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario autenticar(String rut, String contrasena)
            throws SQLException {

        String sql = """
                SELECT id, nombre, rut, correo, rol
                FROM usuarios
                WHERE rut = ? AND `contraseña` = ?
                LIMIT 1
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, rut);
            ps.setString(2, contrasena);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirUsuario(rs);
                }
            }
        }

        return null;
    }

    public List<Usuario> listar() throws SQLException {

        List<Usuario> usuarios = new ArrayList<>();

        String sql = """
                SELECT id, nombre, rut, correo, rol
                FROM usuarios ORDER BY id
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(construirUsuario(rs));
            }
        }

        return usuarios;
    }

    public Usuario buscarPorId(int id) throws SQLException {

        String sql = """
                SELECT id, nombre, rut, correo, rol
                FROM usuarios WHERE id = ?
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirUsuario(rs);
                }
            }
        }

        return null;
    }

    public int crear(Usuario usuario) throws SQLException {

        String sql = """
                INSERT INTO usuarios
                (nombre, rut, correo, `contraseña`, rol)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getRut());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getContrasena());
            ps.setString(5, usuario.getRol());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    usuario.setId(id);
                    return id;
                }
            }

            throw new SQLException(
                    "No se pudo obtener el ID del usuario.");
        }
    }

    public boolean actualizar(Usuario usuario)
            throws SQLException {

        String sql = """
                UPDATE usuarios
                SET nombre = ?, rut = ?, correo = ?, rol = ?
                WHERE id = ?
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getRut());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getRol());
            ps.setInt(5, usuario.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    private Usuario construirUsuario(ResultSet rs)
            throws SQLException {

        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("rut"),
                rs.getString("correo"),
                null,
                rs.getString("rol")
        );
    }
}
