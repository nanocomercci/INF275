package com.trazapm.control.exportacion;

import com.trazapm.control.Historial;
import com.trazapm.control.ReporteStock;
import com.trazapm.excepciones.ExportacionException;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.UnidadProducto;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera archivos CSV legibles por Excel en configuración regional argentina:
 * separador punto y coma, codificación UTF-8 con BOM y comillas cuando el
 * texto lo requiere. Cada archivo indica la fecha de generación, de modo que
 * el reporte sea reproducible.
 */
public class ExportadorCsv implements Exportador {

    private static final char SEPARADOR = ';';
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String getExtension() {
        return "csv";
    }

    @Override
    public void exportarHistorial(Historial h, Path destino) throws ExportacionException {
        UnidadProducto u = h.getUnidad();
        // try-with-resources: el archivo se cierra aunque ocurra un error al escribir.
        try (BufferedWriter w = abrir(destino)) {
            linea(w, "TrazaPM - Historial de trazabilidad", "Generado " + FECHA_HORA.format(LocalDateTime.now()));
            linea(w, "GTIN", u.getProducto().getGtin());
            linea(w, "Serie", u.getNumeroSerie());
            linea(w, "Producto", u.getProducto().getDescripcion());
            linea(w, "Estado actual", u.getEstado().getEtiqueta());
            w.newLine();
            linea(w, "Fecha y hora", "Evento", "Origen", "Destino", "Documento", "Usuario", "Envío SNT-PM");
            for (Evento ev : h.getEventos()) {
                linea(w, FECHA_HORA.format(ev.getFechaHora()), ev.getTipo().getNombre(),
                        Historial.origenDe(ev), Historial.destinoDe(ev),
                        ev.getDocumento(), ev.getUsuario().getNombreUsuario(), ev.getEstadoEnvio().name());
            }
        } catch (IOException e) {
            throw new ExportacionException("No se pudo guardar el archivo " + destino.getFileName()
                    + ". Verifique que no esté abierto en otra aplicación y que la carpeta permita escribir.", e);
        }
    }

    @Override
    public void exportarStock(List<UnidadProducto> unidades, ReporteStock r, Path destino) throws ExportacionException {
        try (BufferedWriter w = abrir(destino)) {
            linea(w, "TrazaPM - Reporte de stock y vencimientos", "Generado " + FECHA_HORA.format(LocalDateTime.now()));
            w.newLine();
            // Resumen: la matriz int[][] se recorre por filas (familias) y columnas (estados).
            String[] encabezado = new String[ReporteStock.ESTADOS.length + 2];
            encabezado[0] = "Familia";
            for (int j = 0; j < ReporteStock.ESTADOS.length; j++) {
                encabezado[j + 1] = ReporteStock.ESTADOS[j].getEtiqueta();
            }
            encabezado[encabezado.length - 1] = "Total";
            linea(w, encabezado);
            for (int i = 0; i < ReporteStock.FAMILIAS.length; i++) {
                String[] fila = new String[encabezado.length];
                fila[0] = ReporteStock.FAMILIAS[i];
                for (int j = 0; j < ReporteStock.ESTADOS.length; j++) {
                    fila[j + 1] = String.valueOf(r.cantidad(i, j));
                }
                fila[fila.length - 1] = String.valueOf(r.totalFamilia(i));
                linea(w, fila);
            }
            w.newLine();
            linea(w, "GTIN", "Familia", "Marca y modelo", "Serie", "Lote", "Vencimiento", "Estado", "Ubicación");
            for (UnidadProducto u : unidades) {
                linea(w, u.getProducto().getGtin(), u.getProducto().getTipoProducto(),
                        u.getProducto().getMarca() + " " + u.getProducto().getModelo(), u.getNumeroSerie(),
                        u.getLote(), u.getVencimiento() == null ? "" : FECHA.format(u.getVencimiento()),
                        u.getEstado().getEtiqueta(),
                        u.getUbicacionActual() == null ? "" : u.getUbicacionActual().getNombre());
            }
        } catch (IOException e) {
            throw new ExportacionException("No se pudo guardar el archivo " + destino.getFileName()
                    + ". Verifique que no esté abierto en otra aplicación y que la carpeta permita escribir.", e);
        }
    }

    /** Abre el archivo en UTF-8 y escribe la marca BOM para que Excel reconozca los acentos. */
    private static BufferedWriter abrir(Path destino) throws IOException {
        BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
        w.write('﻿');
        return w;
    }

    private static void linea(BufferedWriter w, String... campos) throws IOException {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                w.write(SEPARADOR);
            }
            w.write(escapar(campos[i]));
        }
        w.newLine();
    }

    /** Encierra entre comillas los valores que contienen el separador, comillas o saltos de línea. */
    static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.indexOf(SEPARADOR) >= 0 || valor.indexOf('"') >= 0 || valor.indexOf('\n') >= 0) {
            return '"' + valor.replace("\"", "\"\"") + '"';
        }
        return valor;
    }
}
