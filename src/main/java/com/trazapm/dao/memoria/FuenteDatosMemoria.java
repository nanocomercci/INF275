package com.trazapm.dao.memoria;

import com.trazapm.dao.CatalogoDAO;
import com.trazapm.dao.EventoDAO;
import com.trazapm.dao.FuenteDatos;
import com.trazapm.dao.ProductoDAO;
import com.trazapm.dao.UnidadProductoDAO;
import com.trazapm.modelo.Agente;
import com.trazapm.modelo.EstadoEnvio;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.Marcapasos;
import com.trazapm.modelo.Producto;
import com.trazapm.modelo.Profesional;
import com.trazapm.modelo.ProtesisCadera;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.StentCoronario;
import com.trazapm.modelo.Ubicacion;
import com.trazapm.modelo.UnidadProducto;
import com.trazapm.modelo.Usuario;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria de todos los DAO. Se usa cuando MySQL no está
 * disponible (modo demostración) y replica los datos sintéticos del script
 * sql/02_datos_prueba.sql. Estructuras utilizadas:
 * - ArrayList: conserva el orden de alta (listados).
 * - HashMap: índice por clave GTIN|serie para búsquedas O(1) y para detectar duplicados.
 */
public class FuenteDatosMemoria implements FuenteDatos {

    private final List<Ubicacion> ubicaciones = new ArrayList<>();
    private final List<Agente> agentes = new ArrayList<>();
    private final List<Profesional> profesionales = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final Map<String, Producto> productosPorGtin = new HashMap<>();
    private final List<UnidadProducto> unidades = new ArrayList<>();
    private final Map<String, UnidadProducto> indiceUnidades = new HashMap<>();
    private final List<Evento> eventos = new ArrayList<>();
    private long secuenciaUnidad = 1;
    private long secuenciaEvento = 1;

    public FuenteDatosMemoria() {
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        ubicaciones.add(new Ubicacion(1L, "Depósito central", "Depósito", true));
        ubicaciones.add(new Ubicacion(2L, "Farmacia de productos médicos", "Farmacia", true));
        ubicaciones.add(new Ubicacion(3L, "Quirófano 1", "Quirófano", true));
        ubicaciones.add(new Ubicacion(4L, "Cuarentena", "Depósito restringido", true));
        agentes.add(new Agente(1L, "Proveedor Médico Demo SA", "30710000001", "7791234000012", "PROVEEDOR"));
        agentes.add(new Agente(2L, "Distribuidora Implantes Demo SRL", "30710000002", "7791234000029", "DISTRIBUIDOR"));
        profesionales.add(new Profesional(1L, "Dra. Laura Sintética", "MP-DEMO-01", "27300000001", true));
        profesionales.add(new Profesional(2L, "Dr. Martín Ejemplo", "MP-DEMO-02", "20300000002", true));
        usuarios.add(new Usuario(1L, "operador.demo", "Operador de Depósito", Rol.OPERADOR, true));
        usuarios.add(new Usuario(2L, "supervisor.demo", "Responsable de Trazabilidad", Rol.SUPERVISOR, true));
        usuarios.add(new Usuario(3L, "profesional.demo", "Dra. Laura Sintética", Rol.PROFESIONAL, true));

        agregarProducto(new Marcapasos(1L, "07791234000012", "CardioDemo", "Pulse S1", "PM-1001", 2));
        agregarProducto(new StentCoronario(2L, "07791234000029", "VascularDemo", "Flow 25", "PM-1002", 2.5, true));
        agregarProducto(new ProtesisCadera(3L, "07791234000036", "OrtoDemo", "Hip Total C", "PM-1003", "Titanio"));

        // Unidad de ejemplo con su evento de recepción, igual que en el script SQL.
        UnidadProducto u = new UnidadProducto(productosPorGtin.get("07791234000012"), "SN-DEMO-0001", "L-2026-01",
                LocalDate.of(2028, 12, 31), ubicaciones.get(0));
        unidades().guardar(u);
        eventos().guardar(new Evento(null, u, com.trazapm.modelo.TipoEvento.RECEPCION, usuarios.get(0), null,
                ubicaciones.get(0), agentes.get(0), null, LocalDateTime.now().minusDays(3), "REM-DEMO-001",
                "Recepción de datos sintéticos", EstadoEnvio.PENDIENTE));
    }

    private void agregarProducto(Producto p) {
        productosPorGtin.put(p.getGtin(), p);
    }

    @Override
    public ProductoDAO productos() {
        return new ProductoDAO() {
            @Override
            public Optional<Producto> buscarPorGtin(String gtin) {
                return Optional.ofNullable(productosPorGtin.get(gtin.trim()));
            }

            @Override
            public List<Producto> listarTodos() {
                return new ArrayList<>(productosPorGtin.values());
            }
        };
    }

    @Override
    public UnidadProductoDAO unidades() {
        return new UnidadProductoDAO() {
            @Override
            public Optional<UnidadProducto> buscarPorGtinYSerie(String gtin, String serie) {
                return Optional.ofNullable(indiceUnidades.get(UnidadProducto.claveDe(gtin, serie)));
            }

            @Override
            public UnidadProducto guardar(UnidadProducto unidad) {
                unidad.setId(secuenciaUnidad++);
                unidades.add(unidad);
                indiceUnidades.put(unidad.getClave(), unidad);
                return unidad;
            }

            @Override
            public void actualizar(UnidadProducto unidad) {
                // En memoria el objeto ya está modificado por referencia; no hay nada que sincronizar.
            }

            @Override
            public List<UnidadProducto> listarTodas() {
                return new ArrayList<>(unidades);
            }
        };
    }

    @Override
    public EventoDAO eventos() {
        return new EventoDAO() {
            @Override
            public Evento guardar(Evento evento) {
                evento.setId(secuenciaEvento++);
                eventos.add(evento);
                return evento;
            }

            @Override
            public List<Evento> listarPorUnidad(Long unidadId) {
                List<Evento> resultado = new ArrayList<>();
                for (Evento e : eventos) {
                    if (e.getUnidad().getId().equals(unidadId)) {
                        resultado.add(e);
                    }
                }
                return resultado;
            }

            @Override
            public List<Evento> listarPendientes() {
                List<Evento> resultado = new ArrayList<>();
                for (Evento e : eventos) {
                    if (e.getEstadoEnvio() == EstadoEnvio.PENDIENTE) {
                        resultado.add(e);
                    }
                }
                return resultado;
            }

            @Override
            public void registrarIntentoEnvio(Evento evento, String codigoExterno) {
                // El estado ya quedó en el objeto; en memoria no se lleva tabla de intentos.
            }
        };
    }

    @Override
    public CatalogoDAO catalogo() {
        return new CatalogoDAO() {
            @Override
            public List<Ubicacion> listarUbicaciones() {
                return Collections.unmodifiableList(ubicaciones);
            }

            @Override
            public List<Agente> listarAgentes() {
                return Collections.unmodifiableList(agentes);
            }

            @Override
            public List<Profesional> listarProfesionales() {
                return Collections.unmodifiableList(profesionales);
            }

            @Override
            public List<Usuario> listarUsuarios() {
                return Collections.unmodifiableList(usuarios);
            }
        };
    }

    @Override
    public <T> T enTransaccion(com.trazapm.dao.Transaccion<T> trabajo)
            throws com.trazapm.excepciones.TrazabilidadException {
        // En memoria no hay rollback real: las validaciones se hacen antes de modificar datos.
        return trabajo.ejecutar();
    }

    @Override
    public String getDescripcion() {
        return "Modo demostración (datos en memoria)";
    }
}
