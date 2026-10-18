package com.trazapm.modelo;

/** Lugar físico interno del establecimiento (depósito, farmacia, quirófano). */
public class Ubicacion extends EntidadBase {

    private String nombre;
    private String sector;
    private boolean activa;

    public Ubicacion(Long id, String nombre, String sector, boolean activa) {
        super(id);
        this.nombre = nombre;
        this.sector = sector;
        this.activa = activa;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSector() {
        return sector;
    }

    public boolean isActiva() {
        return activa;
    }

    /** Indica si la ubicación es el área restringida de cuarentena. */
    public boolean esCuarentena() {
        return nombre.toLowerCase().contains("cuarentena");
    }

    @Override
    public String toString() {
        return nombre;
    }
}
