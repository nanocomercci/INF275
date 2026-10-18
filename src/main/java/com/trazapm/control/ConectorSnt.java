package com.trazapm.control;

import com.trazapm.excepciones.IntegracionException;
import com.trazapm.modelo.Evento;

/**
 * Contrato del adaptador hacia el Sistema Nacional de Trazabilidad de
 * Productos Médicos. Hoy existe solo la implementación simulada; una
 * implementación real (servicio web de ANMAT) podría reemplazarla sin
 * modificar ServicioIntegracion.
 */
public interface ConectorSnt {

    /** Informa el evento; lanza IntegracionException si el servicio no responde. */
    RespuestaSnt informar(Evento evento) throws IntegracionException;

    boolean isDisponible();
}
