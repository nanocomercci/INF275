package com.trazapm.control;

import com.trazapm.control.operaciones.OperacionCuarentena;
import com.trazapm.control.operaciones.OperacionDevolucion;
import com.trazapm.control.operaciones.OperacionImplantacion;
import com.trazapm.control.operaciones.OperacionLevantarCuarentena;
import com.trazapm.control.operaciones.OperacionTraslado;
import com.trazapm.control.operaciones.OperacionTrazabilidad;
import com.trazapm.dao.FuenteDatos;
import com.trazapm.excepciones.TrazabilidadException;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.UnidadProducto;
import com.trazapm.util.Algoritmos;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Clase de control de los movimientos (CU-02, CU-03, CU-04 y CU-09), la consulta de
 * trazabilidad (CU-05) y los reportes de stock y vencimientos (CU-08).
 */
public class ServicioTrazabilidad {

    private final FuenteDatos datos;
    private final ServicioIntegracion integracion;
    private final List<OperacionTrazabilidad> operaciones = new ArrayList<>();

    public ServicioTrazabilidad(FuenteDatos datos, ServicioIntegracion integracion) {
        this.datos = datos;
        this.integracion = integracion;
        // Lista polimórfica: todas se manejan como OperacionTrazabilidad.
        operaciones.add(new OperacionTraslado());
        operaciones.add(new OperacionCuarentena());
        operaciones.add(new OperacionLevantarCuarentena());
        operaciones.add(new OperacionImplantacion());
        operaciones.add(new OperacionDevolucion());
    }

    public List<OperacionTrazabilidad> getOperaciones() {
        return operaciones;
    }

    /**
     * Registra cualquier movimiento sin conocer su tipo concreto: la
     * operación recibida decide sus reglas, su estado resultante y su
     * ubicación final (enlace dinámico).
     */
    public Evento registrarMovimiento(OperacionTrazabilidad operacion, SolicitudMovimiento s) throws TrazabilidadException {
        operacion.validar(s);
        UnidadProducto unidad = s.getUnidad();
        Evento evento = operacion.crearEvento(s);
        EstadoUnidad estadoAnterior = unidad.getEstado();
        com.trazapm.modelo.Ubicacion ubicacionAnterior = unidad.getUbicacionActual();
        try {
            datos.enTransaccion(() -> {
                unidad.setEstado(operacion.estadoResultante());
                unidad.setUbicacionActual(operacion.ubicacionResultante(s));
                datos.unidades().actualizar(unidad);
                return datos.eventos().guardar(evento);
            });
        } catch (TrazabilidadException | RuntimeException e) {
            // Si la base revirtió, el objeto en memoria también vuelve atrás.
            unidad.setEstado(estadoAnterior);
            unidad.setUbicacionActual(ubicacionAnterior);
            throw e;
        }
        integracion.encolar(evento);
        return evento;
    }

    public Historial consultarHistorial(String gtin, String serie) throws TrazabilidadException {
        if (gtin == null || gtin.isBlank() || serie == null || serie.isBlank()) {
            throw new TrazabilidadException("Ingrese GTIN y número de serie.");
        }
        Optional<UnidadProducto> unidad = datos.unidades().buscarPorGtinYSerie(gtin, serie);
        if (unidad.isEmpty()) {
            throw new TrazabilidadException("No existe una unidad con GTIN " + gtin.trim() + " y serie "
                    + serie.trim().toUpperCase() + ".");
        }
        List<Evento> eventos = Algoritmos.ordenarPorInsercion(
                datos.eventos().listarPorUnidad(unidad.get().getId()),
                Comparator.comparing(Evento::getFechaHora).thenComparing(Evento::getId));
        return new Historial(unidad.get(), eventos);
    }

    public List<UnidadProducto> listarUnidades() {
        return datos.unidades().listarTodas();
    }

    /** Regla trasladada desde la entidad: vigencia de una unidad a una fecha. */
    public boolean estaVigente(UnidadProducto u, LocalDate fecha) {
        return u.getVencimiento() == null || !u.getVencimiento().isBefore(fecha);
    }

    /** Días que faltan para el vencimiento (negativo si ya venció). */
    public long diasParaVencer(UnidadProducto u, LocalDate fecha) {
        return u.getVencimiento() == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(fecha, u.getVencimiento());
    }

    /**
     * Unidades en stock (no finales) dentro del umbral de alerta de su
     * familia, ordenadas de la más próxima a la más lejana a vencer.
     * El umbral se obtiene en forma polimórfica: getDiasAlertaVencimiento().
     */
    public List<UnidadProducto> proximasAVencer(LocalDate fecha) {
        List<UnidadProducto> enAlerta = new ArrayList<>();
        for (UnidadProducto u : listarUnidades()) {
            if (!u.getEstado().esFinal()
                    && diasParaVencer(u, fecha) <= u.getProducto().getDiasAlertaVencimiento()) {
                enAlerta.add(u);
            }
        }
        return Algoritmos.ordenarPorInsercion(enAlerta, Comparator.comparing(UnidadProducto::getVencimiento));
    }

    /** Ordena por serie y aplica búsqueda binaria (CU-08, búsqueda rápida). */
    public Optional<UnidadProducto> buscarPorSerie(String serie) {
        List<UnidadProducto> ordenadas = Algoritmos.ordenarPorInsercion(listarUnidades(),
                Comparator.comparing(UnidadProducto::getNumeroSerie));
        int pos = Algoritmos.busquedaBinaria(ordenadas, serie.trim().toUpperCase(), UnidadProducto::getNumeroSerie);
        return pos >= 0 ? Optional.of(ordenadas.get(pos)) : Optional.empty();
    }

    /** Tablero de stock por familia y estado (matriz int[][]), CU-08. */
    public ReporteStock generarReporteStock() {
        return new ReporteStock(listarUnidades());
    }

    /** Conteo de unidades por estado para el panel de inicio. */
    public Map<EstadoUnidad, Integer> contarPorEstado() {
        Map<EstadoUnidad, Integer> conteo = new EnumMap<>(EstadoUnidad.class);
        for (EstadoUnidad e : EstadoUnidad.values()) {
            conteo.put(e, 0);
        }
        for (UnidadProducto u : listarUnidades()) {
            conteo.put(u.getEstado(), conteo.get(u.getEstado()) + 1);
        }
        return conteo;
    }
}
