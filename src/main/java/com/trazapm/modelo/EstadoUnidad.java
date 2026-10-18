package com.trazapm.modelo;

/** Estados posibles de una unidad (columna unidad_producto.estado_actual). */
public enum EstadoUnidad {
    DISPONIBLE("Disponible"),
    CUARENTENA("En cuarentena"),
    IMPLANTADA("Implantada"),
    DEVUELTA("Devuelta"),
    DESTRUIDA("Destruida"),
    EXTRAVIADA("Extraviada");

    private final String etiqueta;

    EstadoUnidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Un estado final ya no admite nuevos movimientos. */
    public boolean esFinal() {
        return this == IMPLANTADA || this == DEVUELTA || this == DESTRUIDA || this == EXTRAVIADA;
    }
}
