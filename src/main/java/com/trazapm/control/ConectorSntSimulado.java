package com.trazapm.control;

import com.trazapm.excepciones.IntegracionException;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.TipoEvento;

/**
 * Simula el servicio externo. Puede "caerse" a pedido desde la interfaz
 * para demostrar la continuidad operativa (RNF de disponibilidad): los
 * eventos quedan en la cola local y se reintentan después.
 */
public class ConectorSntSimulado implements ConectorSnt {

    private boolean disponible = true;
    private int secuencia = 1000;

    @Override
    public RespuestaSnt informar(Evento evento) throws IntegracionException {
        if (!disponible) {
            throw new IntegracionException("SNT-PM sin respuesta (tiempo de espera agotado).");
        }
        // Regla simulada del servicio: los eventos con efecto regulatorio exigen documento respaldatorio.
        boolean exigeDocumento = evento.getTipo() == TipoEvento.RECEPCION
                || evento.getTipo() == TipoEvento.IMPLANTACION
                || evento.getTipo() == TipoEvento.DEVOLUCION;
        if (exigeDocumento && (evento.getDocumento() == null || evento.getDocumento().isEmpty())) {
            return new RespuestaSnt(false, null, "Rechazado: falta documento respaldatorio.");
        }
        secuencia++;
        return new RespuestaSnt(true, "SNT-" + secuencia, "Transacción aceptada.");
    }

    @Override
    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
