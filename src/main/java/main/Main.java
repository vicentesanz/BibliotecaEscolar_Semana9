
package main;

import dao.LibroDAO;
import dao.EstudianteDAO;
import dao.CategoriaDAO;

import modelo.Libro;
import modelo.Estudiante;
import modelo.Categoria;

import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== Biblioteca Escolar - Semana 9 ===");

        LibroDAO libroDAO = new LibroDAO();
        EstudianteDAO estudianteDAO = new EstudianteDAO();
        CategoriaDAO categoriaDAO = new CategoriaDAO();

        try {

            List<Libro> libros = libroDAO.listar();
            List<Estudiante> estudiantes = estudianteDAO.listar();
            List<Categoria> categorias = categoriaDAO.listar();

            System.out.println("Conexion a MySQL correcta.");
            System.out.println();

            System.out.println("=== LIBROS ===");
            for (Libro libro : libros) {
                System.out.println(
                        libro.getId() + ". " + libro.getTitulo()
                                + " | Stock: " + libro.getStock()
                );
            }

            System.out.println();
            System.out.println("=== ESTUDIANTES ===");

            for (Estudiante estudiante : estudiantes) {
                System.out.println(
                        estudiante.getId() + ". "
                                + estudiante.getNombre()
                                + " | Curso: " + estudiante.getCurso()
                );
            }

            System.out.println();
            System.out.println("=== CATEGORIAS ===");

            for (Categoria categoria : categorias) {
                System.out.println(
                        categoria.getId() + ". "
                                + categoria.getNombre()
                );
            }

            System.out.println();
            System.out.println("=== RESUMEN ===");
            System.out.println("Libros: " + libros.size());
            System.out.println("Estudiantes: " + estudiantes.size());
            System.out.println("Categorias: " + categorias.size());

        } catch (SQLException e) {

            System.err.println(
                    "Error al consultar la biblioteca: "
                            + e.getMessage()
            );

            throw new IllegalStateException(
                    "No se pudieron consultar los datos.", e
            );
        }
    }
}
