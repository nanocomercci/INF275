package com.trazapm.control.operaciones;

import com.trazapm.control.SolicitudMovimiento;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.TipoEvento;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Libera una unidad de cuarentena. Solo el supervisor puede autorizarlo. */
public class OperacionLevantarCuarentena extends OperacionTrazabilidad {

    @Override
    public TipoEvento getTipoEvento() {
        return TipoEvento.LEVANTAR_CUARENTENA;
    }

    @Override
    protected Set<EstadoUnidad> estadosOrigenPermitidos() {
        return EnumSet.of(EstadoUnidad.CUARENTENA);
    }

    @Override
    public EstadoUnidad estadoResultante() {
        return EstadoUnidad.DISPONIBLE;
    }

    @Override
    protected Set<Rol> rolesPermitidos() {
        return EnumSet.of(Rol.SUPERVISOR, Rol.ADMINISTRADOR);
    }

    @Override
    protected void validarEspecifico(SolicitudMovimiento s, List<String> errores) {
        exigirDestinoDistinto(s, errores);
        if (s.getMotivo().isEmpty()) {
            errores.add("Debe registrar el fundamento de la liberación.");
        }
    }

    @Override
    public boolean usaDestino() {
        return true;
    }
}
