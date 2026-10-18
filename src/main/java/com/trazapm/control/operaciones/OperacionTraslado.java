package com.trazapm.control.operaciones;

import com.trazapm.control.SolicitudMovimiento;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.TipoEvento;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Traslado interno entre ubicaciones habilitadas (la unidad sigue DISPONIBLE). */
public class OperacionTraslado extends OperacionTrazabilidad {

    @Override
    public TipoEvento getTipoEvento() {
        return TipoEvento.TRASLADO;
    }

    @Override
    protected Set<EstadoUnidad> estadosOrigenPermitidos() {
        return EnumSet.of(EstadoUnidad.DISPONIBLE);
    }

    @Override
    public EstadoUnidad estadoResultante() {
        return EstadoUnidad.DISPONIBLE;
    }

    @Override
    protected Set<Rol> rolesPermitidos() {
        return EnumSet.of(Rol.OPERADOR, Rol.SUPERVISOR, Rol.ADMINISTRADOR);
    }

    @Override
    protected void validarEspecifico(SolicitudMovimiento s, List<String> errores) {
        exigirDestinoDistinto(s, errores);
        if (s.getDestino() != null && s.getDestino().esCuarentena()) {
            errores.add("Para enviar a cuarentena use el evento \"Puesta en cuarentena\".");
        }
    }

    @Override
    public boolean usaDestino() {
        return true;
    }
}
