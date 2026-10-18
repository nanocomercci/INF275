package com.trazapm.excepciones;

import com.trazapm.modelo.UnidadProducto;

/** Regla RN-01: no puede existir dos veces la misma combinación GTIN + serie. */
public class UnidadDuplicadaException extends TrazabilidadException {

    private static final long serialVersionUID = 1L;

    private final transient UnidadProducto existente;

    public UnidadDuplicadaException(UnidadProducto existente) {
        super("La unidad " + existente.getProducto().getGtin() + " / " + existente.getNumeroSerie()
                + " ya está registrada. Estado actual: " + existente.getEstado().getEtiqueta()
                + " en " + existente.getUbicacionActual() + ".");
        this.existente = existente;
    }

    public UnidadProducto getExistente() {
        return existente;
    }
}
