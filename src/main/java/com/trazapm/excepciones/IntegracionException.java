package com.trazapm.excepciones;

/** El conector con el SNT-PM (simulado) no está disponible. El evento queda pendiente. */
public class IntegracionException extends TrazabilidadException {

    private static final long serialVersionUID = 1L;

    public IntegracionException(String mensaje) {
        super(mensaje);
    }
}
