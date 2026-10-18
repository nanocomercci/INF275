package com.trazapm.control.operaciones;

import com.trazapm.control.SolicitudMovimiento;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.TipoEvento;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Bloquea una unidad sospechada (alerta sanitaria, envase dañado) en el área de cuarentena. */
public class OperacionCuarentena extends OperacionTrazabilidad {

    @Override
    public TipoEvento getTipoEvento() {
        return TipoEvento.CUARENTENA;
    }

    @Override
    protected Set<EstadoUnidad> estadosOrigenPermitidos() {
        return EnumSet.of(EstadoUnidad.DISPONIBLE);
    }

    @Override
    public EstadoUnidad estadoResultante() {
        return EstadoUnidad.CUARENTENA;
    }

    @Override
    protected Set<Rol> rolesPermitidos() {
        return EnumSet.of(Rol.OPERADOR, Rol.SUPERVISOR, Rol.ADMINISTRADOR);
    }

    @Override
    protected void validarEspecifico(SolicitudMovimiento s, List<String> errores) {
        if (s.getDestino() == null || !s.getDestino().esCuarentena()) {
            errores.add("El destino debe ser el área de Cuarentena.");
        }
        if (s.getMotivo().isEmpty()) {
            errores.add("Debe indicar el motivo de la cuarentena.");
        }
    }

    @Override
    public boolean usaDestino() {
        return true;
    }
}
