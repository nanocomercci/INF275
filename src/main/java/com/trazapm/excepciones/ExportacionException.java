package com.trazapm.excepciones;

/**
 * Error al generar un archivo de exportación (por ejemplo, una IOException
 * por falta de permisos o un archivo abierto en otra aplicación). Hereda de
 * TrazabilidadException para que la vista la trate igual que el resto de los
 * errores esperables: se informa al usuario y la aplicación continúa.
 */
public class ExportacionException extends TrazabilidadException {

    private static final long serialVersionUID = 1L;

    public ExportacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
