package com.trazapm.dao;

import com.trazapm.excepciones.TrazabilidadException;

/**
 * Bloque de trabajo que debe confirmarse o revertirse completo
 * (por ejemplo: alta de unidad + evento de recepción).
 */
@FunctionalInterface
public interface Transaccion<T> {

    T ejecutar() throws TrazabilidadException;
}
