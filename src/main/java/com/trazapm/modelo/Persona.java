package com.trazapm.modelo;

/**
 * Abstracción de cualquier persona que interviene en la trazabilidad.
 * Usuario (quien opera el sistema) y Profesional (quien implanta) heredan
 * de esta clase y redefinen {@link #getDescripcionRol()} (polimorfismo).
 */
public abstract class Persona extends EntidadBase {

    private String nombreCompleto;

    protected Persona(Long id, String nombreCompleto) {
        super(id);
        this.nombreCompleto = nombreCompleto;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    /** Cada subclase indica cómo se presenta su rol en pantallas y reportes. */
    public abstract String getDescripcionRol();

    @Override
    public String toString() {
        return nombreCompleto + " (" + getDescripcionRol() + ")";
    }
}
