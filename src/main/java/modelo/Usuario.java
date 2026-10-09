
package modelo;

public class Usuario extends Persona {

    private String contrasena;
    private String rol;

    public Usuario(int id, String nombre, String rut,
                   String correo, String contrasena, String rol) {

        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public Usuario(String nombre, String rut, String correo,
                   String contrasena, String rol) {

        this(0, nombre, rut, correo, contrasena, rol);
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String getTipoPersona() {
        return rol;
    }
}
