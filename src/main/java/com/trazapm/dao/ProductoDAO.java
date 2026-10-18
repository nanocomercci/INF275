package com.trazapm.dao;

import com.trazapm.modelo.Producto;
import java.util.List;
import java.util.Optional;

/** Acceso a datos del catálogo de productos (tabla producto). */
public interface ProductoDAO {

    Optional<Producto> buscarPorGtin(String gtin);

    List<Producto> listarTodos();
}
