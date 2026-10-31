package com.trazapm.control.exportacion;

import com.trazapm.control.Historial;
import com.trazapm.control.ReporteStock;
import com.trazapm.excepciones.ExportacionException;
import com.trazapm.modelo.UnidadProducto;
import java.nio.file.Path;
import java.util.List;

/**
 * Contrato de exportación de reportes (patrón Strategy). La vista solo conoce
 * esta interfaz; hoy existe la implementación CSV y mañana podría agregarse
 * una PDF o XLSX sin modificar las pantallas.
 */
public interface Exportador {

    /** Extensión de archivo que genera (sin punto), para el diálogo de guardado. */
    String getExtension();

    /** Exporta el historial de una unidad (CU-05). */
    void exportarHistorial(Historial historial, Path destino) throws ExportacionException;

    /** Exporta el listado de stock y el resumen por familia y estado (CU-08). */
    void exportarStock(List<UnidadProducto> unidades, ReporteStock resumen, Path destino) throws ExportacionException;
}
