package com.trazapm.control.operaciones;

import com.trazapm.control.SolicitudMovimiento;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.TipoEvento;
import com.trazapm.modelo.Ubicacion;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Registra la aplicación de la unidad al paciente. Es un estado final: la
 * unidad sale del stock, por eso no queda con ubicación interna.
 */
public class OperacionImplantacion extends OperacionTrazabilidad {

    @Override
    public TipoEvento getTipoEvento() {
        return TipoEvento.IMPLANTACION;
    }

    @Override
    protected Set<EstadoUnidad> estadosOrigenPermitidos() {
        return EnumSet.of(EstadoUnidad.DISPONIBLE);
    }

    @Override
    public EstadoUnidad estadoResultante() {
        return EstadoUnidad.IMPLANTADA;
    }

    @Override
    protected Set<Rol> rolesPermitidos() {
        return EnumSet.of(Rol.PROFESIONAL, Rol.SUPERVISOR);
    }

    @Override
    protected void validarEspecifico(SolicitudMovimiento s, List<String> errores) {
        if (s.getProfesional() == null) {
            errores.add("Debe indicar el profesional que realiza la implantación.");
        } else if (!s.getProfesional().isHabilitado()) {
            errores.add("El profesional seleccionado no está habilitado.");
        }
        LocalDate vencimiento = s.getUnidad().getVencimiento();
        if (vencimiento != null && vencimiento.isBefore(LocalDate.now())) {
            errores.add("La unidad está vencida desde " + vencimiento + " y no puede implantarse.");
        }
        if (s.getDocumento().isEmpty()) {
            errores.add("Debe indicar el número de parte quirúrgico.");
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
    public boolean usaProfesional() {
        return true;
    }
}
