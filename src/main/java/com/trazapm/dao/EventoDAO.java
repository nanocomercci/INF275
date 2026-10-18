package com.trazapm.dao;

import com.trazapm.modelo.Evento;
import java.util.List;

/** Acceso a datos de eventos (tablas evento y transaccion_externa). */
public interface EventoDAO {

    Evento guardar(Evento evento);

    List<Evento> listarPorUnidad(Long unidadId);

    List<Evento> listarPendientes();

    /** Persiste el resultado de un intento de envío al SNT-PM. */
    void registrarIntentoEnvio(Evento evento, String codigoExterno);
}
