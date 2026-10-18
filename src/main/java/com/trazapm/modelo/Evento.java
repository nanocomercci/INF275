package com.trazapm.modelo;

import java.time.LocalDateTime;

/**
 * Registro inmutable de un hecho de trazabilidad sobre una unidad. Los datos
 * del hecho se fijan en el constructor (no hay setters para ellos); solo
 * cambia el estado de envío cuando se sincroniza con el SNT-PM simulado.
 */
public class Evento extends EntidadBase {

    private final UnidadProducto unidad;
    private final TipoEvento tipo;
    private final Usuario usuario;
    private final Ubicacion ubicacionOrigen;
    private final Ubicacion ubicacionDestino;
    private final Agente agente;
    private final Profesional profesional;
    private final LocalDateTime fechaHora;
    private final String documento;
    private final String motivo;
    private EstadoEnvio estadoEnvio;
    private int intentosEnvio;
    private String respuestaExterna;

    public Evento(Long id, UnidadProducto unidad, TipoEvento tipo, Usuario usuario, Ubicacion ubicacionOrigen,
                  Ubicacion ubicacionDestino, Agente agente, Profesional profesional, LocalDateTime fechaHora,
                  String documento, String motivo, EstadoEnvio estadoEnvio) {
        super(id);
        this.unidad = unidad;
        this.tipo = tipo;
        this.usuario = usuario;
        this.ubicacionOrigen = ubicacionOrigen;
        this.ubicacionDestino = ubicacionDestino;
        this.agente = agente;
        this.profesional = profesional;
        this.fechaHora = fechaHora;
        this.documento = documento;
        this.motivo = motivo;
        this.estadoEnvio = estadoEnvio;
    }

    public UnidadProducto getUnidad() {
        return unidad;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Ubicacion getUbicacionOrigen() {
        return ubicacionOrigen;
    }

    public Ubicacion getUbicacionDestino() {
        return ubicacionDestino;
    }

    public Agente getAgente() {
        return agente;
    }

    public Profesional getProfesional() {
        return profesional;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getDocumento() {
        return documento;
    }

    public String getMotivo() {
        return motivo;
    }

    public EstadoEnvio getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(EstadoEnvio estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public int getIntentosEnvio() {
        return intentosEnvio;
    }

    public void setIntentosEnvio(int intentosEnvio) {
        this.intentosEnvio = intentosEnvio;
    }

    public String getRespuestaExterna() {
        return respuestaExterna;
    }

    public void setRespuestaExterna(String respuestaExterna) {
        this.respuestaExterna = respuestaExterna;
    }
}
