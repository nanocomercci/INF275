package com.trazapm.control;

import com.trazapm.modelo.Agente;
import java.time.LocalDate;

/** Datos que el operador ingresa en la pantalla de recepción (CU-01). */
public class DatosRecepcion {

    private final String gtin;
    private final String numeroSerie;
    private final String lote;
    private final LocalDate vencimiento;
    private final Agente proveedor;
    private final String documento;

    public DatosRecepcion(String gtin, String numeroSerie, String lote, LocalDate vencimiento,
                          Agente proveedor, String documento) {
        this.gtin = limpiar(gtin);
        this.numeroSerie = limpiar(numeroSerie).toUpperCase();
        this.lote = limpiar(lote);
        this.vencimiento = vencimiento;
        this.proveedor = proveedor;
        this.documento = limpiar(documento);
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public String getGtin() {
        return gtin;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public String getLote() {
        return lote;
    }

    public LocalDate getVencimiento() {
        return vencimiento;
    }

    public Agente getProveedor() {
        return proveedor;
    }

    public String getDocumento() {
        return documento;
    }
}
