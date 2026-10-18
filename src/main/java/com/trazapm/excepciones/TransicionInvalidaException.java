package com.trazapm.excepciones;

import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.TipoEvento;

/** El evento solicitado no es compatible con el estado actual de la unidad. */
public class TransicionInvalidaException extends TrazabilidadException {

    private static final long serialVersionUID = 1L;

    public TransicionInvalidaException(TipoEvento evento, EstadoUnidad estadoActual) {
        super("No se puede registrar \"" + evento.getNombre() + "\" sobre una unidad en estado "
                + estadoActual.getEtiqueta() + ".");
    }
}
