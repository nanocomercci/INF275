package com.trazapm.excepciones;

/**
 * Excepción verificada (checked) base de las reglas de negocio. Al ser
 * checked, el compilador obliga a la capa de vista a capturarla y mostrar
 * el mensaje al usuario, en lugar de que la aplicación se interrumpa.
 */
public class TrazabilidadException extends Exception {

    private static final long serialVersionUID = 1L;

    public TrazabilidadException(String mensaje) {
        super(mensaje);
    }

    public TrazabilidadException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
