package com.trazapm.control.operaciones;

import com.trazapm.control.SolicitudMovimiento;
import com.trazapm.excepciones.DatosInvalidosException;
import com.trazapm.excepciones.PermisoDenegadoException;
import com.trazapm.excepciones.TransicionInvalidaException;
import com.trazapm.excepciones.TrazabilidadException;
import com.trazapm.modelo.EstadoEnvio;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.TipoEvento;
import com.trazapm.modelo.Ubicacion;
import com.trazapm.modelo.UnidadProducto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Clase abstracta que define el algoritmo común a todo movimiento de una
 * unidad (patrón Método Plantilla): verificar permiso, verificar que el
 * estado actual admite el evento, validar datos propios y construir el
 * evento. Cada subclase redefine solo lo que la distingue (polimorfismo):
 * estados de origen, estado resultante, roles y validaciones específicas.
 *
 * Así las reglas de cada evento quedan en la capa de control y no en las
 * entidades, como se pidió en la devolución del TP2.
 */
public abstract class OperacionTrazabilidad {

    /** Tipo de evento que esta operación registra. */
    public abstract TipoEvento getTipoEvento();

    /** Estados desde los cuales se admite el evento. */
    protected abstract Set<EstadoUnidad> estadosOrigenPermitidos();

    /** Estado en que queda la unidad luego del evento. */
    public abstract EstadoUnidad estadoResultante();

    /** Roles habilitados para registrar el evento. */
    protected abstract Set<Rol> rolesPermitidos();

    /** Validaciones propias del evento; agregan mensajes a la lista. */
    protected abstract void validarEspecifico(SolicitudMovimiento s, List<String> errores);

    /**
     * Ubicación en que queda la unidad. Por defecto, el destino elegido;
     * las operaciones que sacan la unidad del establecimiento devuelven null.
     */
    public Ubicacion ubicacionResultante(SolicitudMovimiento s) {
        return s.getDestino();
    }

    /** Indica a la vista qué campos del formulario corresponde habilitar. */
    public abstract boolean usaDestino();

    public boolean usaAgente() {
        return false;
    }

    public boolean usaProfesional() {
        return false;
    }

    /**
     * Método plantilla: el orden de las validaciones es fijo (final) y no
     * puede alterarse desde las subclases.
     */
    public final void validar(SolicitudMovimiento s) throws TrazabilidadException {
        if (!rolesPermitidos().contains(s.getUsuario().getRol())) {
            throw new PermisoDenegadoException("El rol " + s.getUsuario().getRol().getDescripcion()
                    + " no puede registrar " + getTipoEvento().getNombre() + ".");
        }
        UnidadProducto unidad = s.getUnidad();
        if (!estadosOrigenPermitidos().contains(unidad.getEstado())) {
            throw new TransicionInvalidaException(getTipoEvento(), unidad.getEstado());
        }
        List<String> errores = new ArrayList<>();
        validarEspecifico(s, errores);
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
    }

    /** Construye el evento con origen = ubicación actual de la unidad. */
    public final Evento crearEvento(SolicitudMovimiento s) {
        return new Evento(null, s.getUnidad(), getTipoEvento(), s.getUsuario(), s.getUnidad().getUbicacionActual(),
                ubicacionResultante(s), s.getAgente(), s.getProfesional(), LocalDateTime.now(),
                s.getDocumento(), s.getMotivo(), EstadoEnvio.PENDIENTE);
    }

    /** Validación reutilizable por varias subclases. */
    protected static void exigirDestinoDistinto(SolicitudMovimiento s, List<String> errores) {
        if (s.getDestino() == null) {
            errores.add("Debe seleccionar la ubicación de destino.");
        } else if (s.getDestino().equals(s.getUnidad().getUbicacionActual())) {
            errores.add("El destino debe ser distinto de la ubicación actual.");
        }
    }

    @Override
    public String toString() {
        return getTipoEvento().getNombre();
    }
}
