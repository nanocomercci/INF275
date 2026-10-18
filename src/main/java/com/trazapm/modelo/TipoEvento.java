package com.trazapm.modelo;

/** Tipos de evento de trazabilidad (tabla tipo_evento, columna codigo). */
public enum TipoEvento {
    RECEPCION("Recepción"),
    TRASLADO("Traslado interno"),
    CUARENTENA("Puesta en cuarentena"),
    LEVANTAR_CUARENTENA("Levantamiento de cuarentena"),
    IMPLANTACION("Implantación"),
    DEVOLUCION("Devolución");

    private final String nombre;

    TipoEvento(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
