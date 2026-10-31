package com.trazapm.control;

import com.trazapm.modelo.Evento;
import com.trazapm.modelo.TipoEvento;
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

    /**
     * Origen legible de un evento: en la recepción es el proveedor; en el resto,
     * la ubicación previa. Lo usan la pantalla y la exportación, para que ambas
     * muestren exactamente lo mismo.
     */
    public static String origenDe(Evento ev) {
        if (ev.getTipo() == TipoEvento.RECEPCION && ev.getAgente() != null) {
            return ev.getAgente().getRazonSocial();
        }
        return ev.getUbicacionOrigen() == null ? "-" : ev.getUbicacionOrigen().getNombre();
    }

    /** Destino legible: el paciente en la implantación, el agente en la devolución o la ubicación nueva. */
    public static String destinoDe(Evento ev) {
        if (ev.getTipo() == TipoEvento.IMPLANTACION) {
            return "Paciente · " + (ev.getProfesional() == null ? "" : ev.getProfesional().getNombreCompleto());
        }
        if (ev.getTipo() == TipoEvento.DEVOLUCION && ev.getAgente() != null) {
            return ev.getAgente().getRazonSocial();
        }
        return ev.getUbicacionDestino() == null ? "-" : ev.getUbicacionDestino().getNombre();
    }
}
