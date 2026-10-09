
package main;

import conexion.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== Biblioteca Escolar - Semana 9 ===");

        String sql = "SELECT COUNT(*) FROM libros";

        try (
                Connection conexion = DatabaseConnection
                        .getInstance()
                        .getConnection();

                PreparedStatement consulta =
                        conexion.prepareStatement(sql);

                ResultSet resultado = consulta.executeQuery()
        ) {

            System.out.println(
                    "Conexión a biblioteca realizada correctamente."
            );

            if (resultado.next()) {
                int cantidad = resultado.getInt(1);

                System.out.println(
                        "Libros registrados: " + cantidad
                );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al conectar con la base de datos: "
                            + e.getMessage()
            );
        }
    }
}
