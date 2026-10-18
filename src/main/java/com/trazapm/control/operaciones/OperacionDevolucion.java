package com.trazapm.control.operaciones;

import com.trazapm.control.SolicitudMovimiento;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.TipoEvento;
import com.trazapm.modelo.Ubicacion;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Devolución de la unidad al proveedor (incluso desde cuarentena). Estado final. */
public class OperacionDevolucion extends OperacionTrazabilidad {

    @Override
    public TipoEvento getTipoEvento() {
        return TipoEvento.DEVOLUCION;
    }

    @Override
    protected Set<EstadoUnidad> estadosOrigenPermitidos() {
        return EnumSet.of(EstadoUnidad.DISPONIBLE, EstadoUnidad.CUARENTENA);
    }

    @Override
    public EstadoUnidad estadoResultante() {
        return EstadoUnidad.DEVUELTA;
    }

    @Override
    protected Set<Rol> rolesPermitidos() {
        return EnumSet.of(Rol.SUPERVISOR, Rol.ADMINISTRADOR);
    }

    @Override
    protected void validarEspecifico(SolicitudMovimiento s, List<String> errores) {
        if (s.getAgente() == null) {
            errores.add("Debe indicar el agente al que se devuelve la unidad.");
        }
        if (s.getMotivo().isEmpty()) {
            errores.add("Debe indicar el motivo de la devolución.");
        }
        if (s.getDocumento().isEmpty()) {
            errores.add("Debe indicar el remito de devolución.");
        }
    }

    @Override
    public Ubicacion ubicacionResultante(SolicitudMovimiento s) {
        return null;
    }

    @Override
    public boolean usaDestino() {
        return false;
    }

    @Override
    public boolean usaAgente() {
        return true;
    }
}
