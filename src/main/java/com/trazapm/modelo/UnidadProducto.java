package com.trazapm.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Unidad física de un producto, identificada en forma unívoca por
 * GTIN + número de serie. Siguiendo la corrección del TP2, la entidad solo
 * conserva su estado (atributos privados + getters/setters): las reglas
 * sobre qué cambios son válidos viven en las clases de control
 * (ControlRecepcion y las subclases de OperacionTrazabilidad).
 */
public class UnidadProducto extends EntidadBase {

    private Producto producto;
    private String numeroSerie;
    private String lote;
    private LocalDate vencimiento;
    private EstadoUnidad estado;
    private Ubicacion ubicacionActual;
    private LocalDateTime fechaAlta;

    public UnidadProducto(Long id, Producto producto, String numeroSerie, String lote, LocalDate vencimiento,
                          EstadoUnidad estado, Ubicacion ubicacionActual, LocalDateTime fechaAlta) {
        super(id);
        this.producto = producto;
        this.numeroSerie = numeroSerie;
        this.lote = lote;
        this.vencimiento = vencimiento;
        this.estado = estado;
        this.ubicacionActual = ubicacionActual;
        this.fechaAlta = fechaAlta;
    }

    /** Constructor para el alta: toda unidad nueva ingresa DISPONIBLE. */
    public UnidadProducto(Producto producto, String numeroSerie, String lote, LocalDate vencimiento, Ubicacion deposito) {
        this(null, producto, numeroSerie, lote, vencimiento, EstadoUnidad.DISPONIBLE, deposito, LocalDateTime.now());
    }

    /** Clave natural usada por los índices en memoria y por la búsqueda. */
    public String getClave() {
        return claveDe(producto.getGtin(), numeroSerie);
    }

    public static String claveDe(String gtin, String serie) {
        return gtin.trim() + "|" + serie.trim().toUpperCase();
    }

    public Producto getProducto() {
        return producto;
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

    public EstadoUnidad getEstado() {
        return estado;
    }

    public void setEstado(EstadoUnidad estado) {
        this.estado = estado;
    }

    public Ubicacion getUbicacionActual() {
        return ubicacionActual;
    }

    public void setUbicacionActual(Ubicacion ubicacionActual) {
        this.ubicacionActual = ubicacionActual;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    @Override
    public String toString() {
        return producto.getGtin() + " / " + numeroSerie + " (" + estado.getEtiqueta() + ")";
    }
}
