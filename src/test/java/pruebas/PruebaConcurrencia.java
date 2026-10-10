
package pruebas;

import conexion.DatabaseConnection;
import dao.CategoriaDAO;
import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;

import modelo.Categoria;
import modelo.Estudiante;
import modelo.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class PruebaConcurrencia {

    public static void main(String[] args) throws Exception {

        int idLibroPrueba = 0;

        System.out.println("=== PRUEBA DE CONCURRENCIA ===");

        try {

            List<Categoria> categorias =
                    new CategoriaDAO().listar();

            List<Estudiante> estudiantes =
                    new EstudianteDAO().listar();

            comprobar(
                    !categorias.isEmpty(),
                    "Se necesita al menos una categoría."
            );

            comprobar(
                    estudiantes.size() >= 2,
                    "Se necesitan al menos dos estudiantes."
            );

            String isbn = "TEST-"
                    + UUID.randomUUID().toString()
                    .substring(0, 12);

            Libro libro = new Libro(
                    "Libro temporal de concurrencia",
                    "Prueba automatizada",
                    isbn,
                    "Editorial temporal",
                    1,
                    categorias.get(0).getId()
            );

            LibroDAO libroDAO = new LibroDAO();

            idLibroPrueba = libroDAO.crear(libro);

            System.out.println(
                    "Libro temporal creado con stock 1."
            );

            final int idLibro = idLibroPrueba;
            final int idEstudiante1 =
                    estudiantes.get(0).getId();
            final int idEstudiante2 =
                    estudiantes.get(1).getId();

            ExecutorService ejecutor =
                    Executors.newFixedThreadPool(2);

            try {

                // PRUEBA 1: DOS PRESTAMOS SIMULTANEOS

                CountDownLatch inicio =
                        new CountDownLatch(1);

                Future<Boolean> solicitud1 = ejecutor.submit(
                        () -> {
                            inicio.await();

                            return new PrestamoDAO()
                                    .registrarPrestamo(
                                            idEstudiante1, idLibro
                                    );
                        }
                );

                Future<Boolean> solicitud2 = ejecutor.submit(
                        () -> {
                            inicio.await();

                            return new PrestamoDAO()
                                    .registrarPrestamo(
                                            idEstudiante2, idLibro
                                    );
                        }
                );

                inicio.countDown();

                boolean resultado1 =
                        solicitud1.get(30, TimeUnit.SECONDS);

                boolean resultado2 =
                        solicitud2.get(30, TimeUnit.SECONDS);

                int prestamosExitosos =
                        (resultado1 ? 1 : 0)
                                + (resultado2 ? 1 : 0);

                Libro libroActual =
                        libroDAO.buscarPorId(idLibro);

                comprobar(
                        prestamosExitosos == 1,
                        "Se esperaba exactamente un préstamo exitoso."
                );

                comprobar(
                        libroActual != null
                                && libroActual.getStock() == 0,
                        "El stock debe quedar en cero."
                );

                int idPrestamo =
                        obtenerPrestamoActivo(idLibro);

                comprobar(
                        idPrestamo > 0,
                        "Debe existir un préstamo activo."
                );

                System.out.println(
                        "[OK] Solo un hilo consiguió el préstamo."
                );

                System.out.println(
                        "[OK] El stock quedó en 0, nunca negativo."
                );

                // PRUEBA 2: DOS DEVOLUCIONES SIMULTANEAS

                CountDownLatch inicioDevoluciones =
                        new CountDownLatch(1);

                Future<Boolean> devolucion1 = ejecutor.submit(
                        () -> {
                            inicioDevoluciones.await();

                            return new PrestamoDAO()
                                    .registrarDevolucion(idPrestamo);
                        }
                );

                Future<Boolean> devolucion2 = ejecutor.submit(
                        () -> {
                            inicioDevoluciones.await();

                            return new PrestamoDAO()
                                    .registrarDevolucion(idPrestamo);
                        }
                );

                inicioDevoluciones.countDown();

                boolean resultadoDevolucion1 =
                        devolucion1.get(30, TimeUnit.SECONDS);

                boolean resultadoDevolucion2 =
                        devolucion2.get(30, TimeUnit.SECONDS);

                int devolucionesExitosas =
                        (resultadoDevolucion1 ? 1 : 0)
                                + (resultadoDevolucion2 ? 1 : 0);

                Libro libroDevuelto =
                        libroDAO.buscarPorId(idLibro);

                comprobar(
                        devolucionesExitosas == 1,
                        "Solo una devolución debe tener éxito."
                );

                comprobar(
                        libroDevuelto != null
                                && libroDevuelto.getStock() == 1,
                        "El stock debe regresar exactamente a 1."
                );

                comprobar(
                        cantidadPrestamosDevueltos(idLibro) == 1,
                        "El préstamo debe quedar registrado como devuelto."
                );

                System.out.println(
                        "[OK] Solo una devolución fue procesada."
                );

                System.out.println(
                        "[OK] El stock regresó exactamente a 1."
                );

                System.out.println();
                System.out.println(
                        "=== TODAS LAS PRUEBAS SUPERADAS ==="
                );

            } finally {

                ejecutor.shutdown();

                if (!ejecutor.awaitTermination(
                        30, TimeUnit.SECONDS)) {

                    ejecutor.shutdownNow();

                    comprobar(
                            ejecutor.awaitTermination(
                                    10, TimeUnit.SECONDS),
                            "Los hilos no terminaron correctamente."
                    );
                }
            }

        } finally {

            if (idLibroPrueba > 0) {

                limpiarDatosPrueba(idLibroPrueba);

                System.out.println(
                        "[OK] Datos temporales eliminados."
                );
            }
        }
    }

    private static int obtenerPrestamoActivo(int idLibro)
            throws SQLException {

        String sql = """
                SELECT id
                FROM prestamos
                WHERE id_libro = ? AND devuelto = FALSE
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, idLibro);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return -1;
                }

                int id = rs.getInt("id");

                comprobar(
                        !rs.next(),
                        "Hay más de un préstamo activo del libro."
                );

                return id;
            }
        }
    }

    private static int cantidadPrestamosDevueltos(int idLibro)
            throws SQLException {

        String sql = """
                SELECT COUNT(*) AS cantidad
                FROM prestamos
                WHERE id_libro = ? AND devuelto = TRUE
                """;

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, idLibro);

            try (ResultSet rs = ps.executeQuery()) {

                rs.next();

                return rs.getInt("cantidad");
            }
        }
    }

    private static void limpiarDatosPrueba(int idLibro)
            throws SQLException {

        try (Connection conexion = DatabaseConnection
                .getInstance().getConnection()) {

            conexion.setAutoCommit(false);

            try {

                String borrarPrestamos = """
                        DELETE FROM prestamos
                        WHERE id_libro = ?
                        """;

                try (PreparedStatement ps =
                             conexion.prepareStatement(
                                     borrarPrestamos)) {

                    ps.setInt(1, idLibro);
                    ps.executeUpdate();
                }

                String borrarLibro = """
                        DELETE FROM libros
                        WHERE id = ?
                        """;

                try (PreparedStatement ps =
                             conexion.prepareStatement(
                                     borrarLibro)) {

                    ps.setInt(1, idLibro);
                    ps.executeUpdate();
                }

                conexion.commit();

            } catch (SQLException ex) {

                conexion.rollback();
                throw ex;
            }
        }
    }

    private static void comprobar(
            boolean condicion, String mensaje) {

        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }
}
