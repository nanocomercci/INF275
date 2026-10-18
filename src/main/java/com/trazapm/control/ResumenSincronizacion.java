package com.trazapm.control;

/** Totales de una corrida de sincronización (CU-07). */
public class ResumenSincronizacion {

    private int aceptados;
    private int rechazados;
    private int fallidos;

    void sumarAceptado() {
        aceptados++;
    }

    void sumarRechazado() {
        rechazados++;
    }

    void sumarFallido() {
        fallidos++;
    }

    public int getAceptados() {
        return aceptados;
    }

    public int getRechazados() {
        return rechazados;
    }

    public int getFallidos() {
        return fallidos;
    }

    @Override
    public String toString() {
        return "Aceptados: " + aceptados + " | Rechazados: " + rechazados + " | Siguen pendientes: " + fallidos;
    }
}
