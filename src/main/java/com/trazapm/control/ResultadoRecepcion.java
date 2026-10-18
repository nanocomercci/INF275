package com.trazapm.control;

import com.trazapm.modelo.Evento;
import com.trazapm.modelo.UnidadProducto;

/** Resultado que ControlRecepcion devuelve a la pantalla (definido en el diseño del TP2). */
public class ResultadoRecepcion {

    private final UnidadProducto unidad;
    private final Evento evento;
    private final String mensaje;

    public ResultadoRecepcion(UnidadProducto unidad, Evento evento, String mensaje) {
        this.unidad = unidad;
        this.evento = evento;
        this.mensaje = mensaje;
    }

    public UnidadProducto getUnidad() {
        return unidad;
    }

    public Evento getEvento() {
        return evento;
    }

    public String getMensaje() {
        return mensaje;
    }
}
