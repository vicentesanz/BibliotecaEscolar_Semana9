
package modelo;

public abstract class Persona implements Identificable {

    private int id;
    private String nombre;
    private String rut;
    private String correo;

    public Persona(int id, String nombre,
                   String rut, String correo) {

        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public abstract String getTipoPersona();
}
