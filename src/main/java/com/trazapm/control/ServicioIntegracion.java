package com.trazapm.control;

import com.trazapm.dao.EventoDAO;
import com.trazapm.excepciones.IntegracionException;
import com.trazapm.modelo.EstadoEnvio;
import com.trazapm.modelo.Evento;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Administra la cola local de eventos pendientes de informar (CU-07).
 * Usa una cola FIFO (Queue/ArrayDeque) porque los eventos de una unidad
 * deben informarse en el mismo orden en que ocurrieron: un traslado no
 * puede llegar al SNT-PM antes que la recepción de esa unidad.
 */
public class ServicioIntegracion {

    private final Queue<Evento> cola = new ArrayDeque<>();
    private final EventoDAO eventoDAO;
    private final ConectorSnt conector;

    public ServicioIntegracion(EventoDAO eventoDAO, ConectorSnt conector) {
        this.eventoDAO = eventoDAO;
        this.conector = conector;
        // Al iniciar, se recuperan los pendientes que quedaron de sesiones anteriores.
        cola.addAll(eventoDAO.listarPendientes());
    }

    public void encolar(Evento evento) {
        cola.offer(evento);
    }

    public List<Evento> getPendientes() {
        return new ArrayList<>(cola);
    }

    public int cantidadPendientes() {
        return cola.size();
    }

    public ConectorSnt getConector() {
        return conector;
    }

    /**
     * Recorre la cola una vez. Cada evento aceptado o rechazado sale de la
     * cola; si el servicio no responde, el evento vuelve al final para el
     * próximo intento y el resto se conserva sin perder el orden.
     */
    public ResumenSincronizacion sincronizar() {
        ResumenSincronizacion resumen = new ResumenSincronizacion();
        int cantidad = cola.size();
        for (int i = 0; i < cantidad; i++) {
            Evento evento = cola.poll();
            evento.setIntentosEnvio(evento.getIntentosEnvio() + 1);
            String codigo = null;
            try {
                RespuestaSnt respuesta = conector.informar(evento);
                codigo = respuesta.getCodigoTransaccion();
                evento.setRespuestaExterna(respuesta.getMensaje());
                if (respuesta.isAceptado()) {
                    evento.setEstadoEnvio(EstadoEnvio.ACEPTADO);
                    resumen.sumarAceptado();
                } else {
                    evento.setEstadoEnvio(EstadoEnvio.RECHAZADO);
                    resumen.sumarRechazado();
                }
            } catch (IntegracionException e) {
                evento.setRespuestaExterna(e.getMessage());
                cola.offer(evento);
                resumen.sumarFallido();
            }
            eventoDAO.registrarIntentoEnvio(evento, codigo);
        }
        return resumen;
    }
}
