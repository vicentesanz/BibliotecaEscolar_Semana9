
package modelo;

import java.time.LocalDate;

public class Prestamo implements Identificable {

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean devuelto;

    public Prestamo(int id, int idEstudiante, int idLibro,
                    LocalDate fechaPrestamo,
                    LocalDate fechaDevolucion,
                    boolean devuelto) {

        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }

    public Prestamo(int idEstudiante, int idLibro,
                    LocalDate fechaPrestamo,
                    LocalDate fechaDevolucion) {

        this(0, idEstudiante, idLibro,
                fechaPrestamo, fechaDevolucion, false);
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public void setDevuelto(boolean devuelto) {
        this.devuelto = devuelto;
    }

    public boolean estaAtrasado() {
        return !devuelto
                && fechaDevolucion != null
                && LocalDate.now().isAfter(fechaDevolucion);
    }

    @Override
    public String toString() {
        return "Prestamo " + id
                + " - Estudiante: " + idEstudiante
                + " - Libro: " + idLibro;
    }
}
