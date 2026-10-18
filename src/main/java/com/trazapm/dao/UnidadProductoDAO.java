package com.trazapm.dao;

import com.trazapm.modelo.UnidadProducto;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de unidades (tabla unidad_producto), tal como se definió en el diseño del TP2. */
public interface UnidadProductoDAO {

    Optional<UnidadProducto> buscarPorGtinYSerie(String gtin, String serie);

    UnidadProducto guardar(UnidadProducto unidad);

    void actualizar(UnidadProducto unidad);

    List<UnidadProducto> listarTodas();
}
