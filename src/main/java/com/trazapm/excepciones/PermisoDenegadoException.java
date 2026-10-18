package com.trazapm.excepciones;

/** El rol del usuario no habilita la operación solicitada. */
public class PermisoDenegadoException extends TrazabilidadException {

    private static final long serialVersionUID = 1L;

    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
