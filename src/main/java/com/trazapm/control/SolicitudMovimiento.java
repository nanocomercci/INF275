package com.trazapm.control;

import com.trazapm.modelo.Agente;
import com.trazapm.modelo.Profesional;
import com.trazapm.modelo.Ubicacion;
import com.trazapm.modelo.UnidadProducto;
import com.trazapm.modelo.Usuario;

/**
 * Datos de un movimiento posterior a la recepción. No todos los campos se
 * usan en todos los eventos: cada operación valida los que necesita.
 */
public class SolicitudMovimiento {

    private final UnidadProducto unidad;
    private final Usuario usuario;
    private final Ubicacion destino;
    private final Agente agente;
    private final Profesional profesional;
    private final String documento;
    private final String motivo;

    public SolicitudMovimiento(UnidadProducto unidad, Usuario usuario, Ubicacion destino, Agente agente,
                               Profesional profesional, String documento, String motivo) {
        this.unidad = unidad;
        this.usuario = usuario;
        this.destino = destino;
        this.agente = agente;
        this.profesional = profesional;
        this.documento = documento == null ? "" : documento.trim();
        this.motivo = motivo == null ? "" : motivo.trim();
    }

    public UnidadProducto getUnidad() {
        return unidad;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Ubicacion getDestino() {
        return destino;
    }

    public Agente getAgente() {
        return agente;
    }

    public Profesional getProfesional() {
        return profesional;
    }

    public String getDocumento() {
        return documento;
    }

    public String getMotivo() {
        return motivo;
    }
}
