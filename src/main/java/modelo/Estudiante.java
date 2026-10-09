
package modelo;

public class Estudiante extends Persona {

    private String curso;

    public Estudiante(int id, String nombre, String rut,
                      String correo, String curso) {

        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    public Estudiante(String nombre, String rut,
                      String correo, String curso) {

        this(0, nombre, rut, correo, curso);
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    @Override
    public String getTipoPersona() {
        return "estudiante";
    }
}
