package com.trazapm.control;

import com.trazapm.modelo.Evento;
import com.trazapm.modelo.UnidadProducto;
import java.util.List;

/** Resultado de la consulta de trazabilidad (CU-05): la unidad y su recorrido. */
public class Historial {

    private final UnidadProducto unidad;
    private final List<Evento> eventos;

    public Historial(UnidadProducto unidad, List<Evento> eventos) {
        this.unidad = unidad;
        this.eventos = eventos;
    }

    public UnidadProducto getUnidad() {
        return unidad;
    }

    public List<Evento> getEventos() {
        return eventos;
    }
}
