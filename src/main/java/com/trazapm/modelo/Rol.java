package com.trazapm.modelo;

/** Roles de usuario definidos en el análisis (tabla rol). */
public enum Rol {
    OPERADOR("Operador de depósito"),
    SUPERVISOR("Responsable de trazabilidad"),
    PROFESIONAL("Profesional implantador"),
    ADMINISTRADOR("Administrador");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
