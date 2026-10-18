package com.trazapm.control;

/** Respuesta del SNT-PM ante un evento informado. */
public class RespuestaSnt {

    private final boolean aceptado;
    private final String codigoTransaccion;
    private final String mensaje;

    public RespuestaSnt(boolean aceptado, String codigoTransaccion, String mensaje) {
        this.aceptado = aceptado;
        this.codigoTransaccion = codigoTransaccion;
        this.mensaje = mensaje;
    }

    public boolean isAceptado() {
        return aceptado;
    }

    public String getCodigoTransaccion() {
        return codigoTransaccion;
    }

    public String getMensaje() {
        return mensaje;
    }
}
