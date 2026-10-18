package com.trazapm.dao;

import com.trazapm.excepciones.TrazabilidadException;

/**
 * Agrupa los DAO de una misma implementación (MySQL o memoria). La interfaz
 * permite que las clases de control trabajen igual con cualquiera de las dos.
 */
public interface FuenteDatos {

    ProductoDAO productos();

    UnidadProductoDAO unidades();

    EventoDAO eventos();

    CatalogoDAO catalogo();

    /**
     * Ejecuta el bloque como una unidad atómica: si se lanza una excepción,
     * se deshacen todos los cambios (rol del GestorTransaccion del diseño).
     */
    <T> T enTransaccion(Transaccion<T> trabajo) throws TrazabilidadException;

    /** Texto que la barra de estado muestra para indicar dónde se guardan los datos. */
    String getDescripcion();
}
