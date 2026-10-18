package com.trazapm.modelo;

/** Profesional de la salud que realiza la implantación. Hereda de Persona. */
public class Profesional extends Persona {

    private String matricula;
    private String cuit;
    private boolean habilitado;

    public Profesional(Long id, String nombreCompleto, String matricula, String cuit, boolean habilitado) {
        super(id, nombreCompleto);
        this.matricula = matricula;
        this.cuit = cuit;
        this.habilitado = habilitado;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getCuit() {
        return cuit;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    @Override
    public String getDescripcionRol() {
        return "Matrícula " + matricula;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + matricula;
    }
}
