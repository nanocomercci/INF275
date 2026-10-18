package com.trazapm.excepciones;

/**
 * Error técnico de acceso a datos (por ejemplo, una SQLException). Es no
 * verificada (unchecked) porque no la provoca el usuario: se propaga hasta
 * la vista, que informa el problema sin cerrar la aplicación.
 */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
