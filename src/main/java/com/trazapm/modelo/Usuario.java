package com.trazapm.modelo;

/** Usuario que inicia sesión y registra eventos. Hereda de Persona. */
public class Usuario extends Persona {

    private String nombreUsuario;
    private Rol rol;
    private boolean activo;

    public Usuario(Long id, String nombreUsuario, String nombreCompleto, Rol rol, boolean activo) {
        super(id, nombreCompleto);
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.activo = activo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String getDescripcionRol() {
        return rol.getDescripcion();
    }

    @Override
    public String toString() {
        return nombreUsuario + " - " + getDescripcionRol();
    }
}
