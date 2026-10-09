
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final DatabaseConnection INSTANCE =
            new DatabaseConnection();

    private static final String URL =
            "jdbc:mysql://localhost:3306/biblioteca";

    private static final String USUARIO = "root";

    private DatabaseConnection() {
    }

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {

        String password =
                System.getenv("BIBLIOTECA_DB_PASSWORD");

        if (password == null) {
            throw new SQLException(
                    "Falta configurar la contraseña de MySQL."
            );
        }

        return DriverManager.getConnection(
                URL, USUARIO, password
        );
    }
}
