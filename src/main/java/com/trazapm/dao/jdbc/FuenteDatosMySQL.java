package com.trazapm.dao.jdbc;

import com.trazapm.dao.CatalogoDAO;
import com.trazapm.dao.EventoDAO;
import com.trazapm.dao.FuenteDatos;
import com.trazapm.dao.ProductoDAO;
import com.trazapm.dao.Transaccion;
import com.trazapm.dao.UnidadProductoDAO;
import com.trazapm.excepciones.PersistenciaException;
import com.trazapm.excepciones.TrazabilidadException;
import com.trazapm.modelo.Agente;
import com.trazapm.modelo.EstadoEnvio;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.Marcapasos;
import com.trazapm.modelo.Producto;
import com.trazapm.modelo.Profesional;
import com.trazapm.modelo.ProtesisCadera;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.StentCoronario;
import com.trazapm.modelo.TipoEvento;
import com.trazapm.modelo.Ubicacion;
import com.trazapm.modelo.UnidadProducto;
import com.trazapm.modelo.Usuario;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación JDBC de los DAO sobre el esquema trazapm del TP2.
 * Todas las sentencias usan PreparedStatement (evita inyección SQL).
 * Las tablas maestras se cargan una vez en mapas id -> objeto para
 * reconstruir las relaciones de unidades y eventos sin repetir consultas.
 */
public class FuenteDatosMySQL implements FuenteDatos {

    private static final String SELECT_UNIDAD =
            "SELECT u.id_unidad, u.id_producto, u.id_ubicacion_actual, u.numero_serie, u.lote, u.vencimiento, "
            + "u.estado_actual, u.fecha_alta FROM unidad_producto u ";

    private final Connection conexion;
    private final Map<Long, Producto> productos = new LinkedHashMap<>();
    private final Map<Long, Ubicacion> ubicaciones = new LinkedHashMap<>();
    private final Map<Long, Agente> agentes = new LinkedHashMap<>();
    private final Map<Long, Profesional> profesionales = new LinkedHashMap<>();
    private final Map<Long, Usuario> usuarios = new LinkedHashMap<>();

    public FuenteDatosMySQL(Connection conexion) throws SQLException {
        this.conexion = conexion;
        cargarCatalogos();
    }

    private void cargarCatalogos() throws SQLException {
        try (Statement st = conexion.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT id_ubicacion, nombre, sector, activa FROM ubicacion ORDER BY id_ubicacion")) {
                while (rs.next()) {
                    ubicaciones.put(rs.getLong(1), new Ubicacion(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id_agente, razon_social, cuit, gln_cufe, tipo_agente FROM agente ORDER BY id_agente")) {
                while (rs.next()) {
                    agentes.put(rs.getLong(1), new Agente(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id_profesional, nombre, matricula, cuit, habilitado FROM profesional ORDER BY id_profesional")) {
                while (rs.next()) {
                    profesionales.put(rs.getLong(1), new Profesional(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBoolean(5)));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT us.id_usuario, us.nombre_usuario, us.nombre_completo, r.nombre, us.activo "
                    + "FROM usuario us JOIN rol r ON r.id_rol = us.id_rol ORDER BY us.id_usuario")) {
                while (rs.next()) {
                    usuarios.put(rs.getLong(1), new Usuario(rs.getLong(1), rs.getString(2), rs.getString(3),
                            Rol.valueOf(rs.getString(4)), rs.getBoolean(5)));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT p.id_producto, tp.nombre, p.gtin, p.marca, p.modelo, p.certificado_anmat, "
                    + "p.camaras, p.diametro_mm, p.liberador_farmaco, p.material FROM producto p "
                    + "JOIN tipo_producto tp ON tp.id_tipo_producto = p.id_tipo_producto ORDER BY p.id_producto")) {
                while (rs.next()) {
                    Producto p = crearProducto(rs);
                    productos.put(p.getId(), p);
                }
            }
        }
    }

    /**
     * Herencia de tabla única: la columna tipo_producto decide qué subclase
     * se instancia, y cada una toma solo sus columnas específicas.
     */
    private Producto crearProducto(ResultSet rs) throws SQLException {
        long id = rs.getLong("id_producto");
        String tipo = rs.getString("nombre");
        String gtin = rs.getString("gtin");
        String marca = rs.getString("marca");
        String modelo = rs.getString("modelo");
        String cert = rs.getString("certificado_anmat");
        switch (tipo) {
            case Marcapasos.TIPO:
                return new Marcapasos(id, gtin, marca, modelo, cert, rs.getInt("camaras"));
            case StentCoronario.TIPO:
                return new StentCoronario(id, gtin, marca, modelo, cert, rs.getDouble("diametro_mm"), rs.getBoolean("liberador_farmaco"));
            case ProtesisCadera.TIPO:
                return new ProtesisCadera(id, gtin, marca, modelo, cert, rs.getString("material"));
            default:
                throw new SQLException("Tipo de producto no contemplado en el prototipo: " + tipo);
        }
    }

    private UnidadProducto mapearUnidad(ResultSet rs) throws SQLException {
        Date venc = rs.getDate("vencimiento");
        Long idUbic = leerId(rs, "id_ubicacion_actual");
        return new UnidadProducto(rs.getLong("id_unidad"), productos.get(rs.getLong("id_producto")),
                rs.getString("numero_serie"), rs.getString("lote"), venc == null ? null : venc.toLocalDate(),
                EstadoUnidad.valueOf(rs.getString("estado_actual")), idUbic == null ? null : ubicaciones.get(idUbic),
                rs.getTimestamp("fecha_alta").toLocalDateTime());
    }

    private Optional<UnidadProducto> buscarUnidadPorId(long id) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(SELECT_UNIDAD + "WHERE u.id_unidad = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearUnidad(rs)) : Optional.empty();
            }
        }
    }

    private static void setIdONulo(PreparedStatement ps, int indice, com.trazapm.modelo.EntidadBase entidad) throws SQLException {
        if (entidad == null) {
            ps.setNull(indice, Types.BIGINT);
        } else {
            ps.setLong(indice, entidad.getId());
        }
    }

    private static Long leerId(ResultSet rs, String columna) throws SQLException {
        long v = rs.getLong(columna);
        return rs.wasNull() ? null : v;
    }

    @Override
    public ProductoDAO productos() {
        return new ProductoDAO() {
            @Override
            public Optional<Producto> buscarPorGtin(String gtin) {
                for (Producto p : productos.values()) {
                    if (p.getGtin().equals(gtin.trim())) {
                        return Optional.of(p);
                    }
                }
                return Optional.empty();
            }

            @Override
            public List<Producto> listarTodos() {
                return new ArrayList<>(productos.values());
            }
        };
    }

    @Override
    public UnidadProductoDAO unidades() {
        return new UnidadProductoDAO() {
            @Override
            public Optional<UnidadProducto> buscarPorGtinYSerie(String gtin, String serie) {
                String sql = SELECT_UNIDAD + "JOIN producto p ON p.id_producto = u.id_producto "
                        + "WHERE p.gtin = ? AND u.numero_serie = ?";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setString(1, gtin.trim());
                    ps.setString(2, serie.trim().toUpperCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? Optional.of(mapearUnidad(rs)) : Optional.empty();
                    }
                } catch (SQLException e) {
                    throw new PersistenciaException("No se pudo buscar la unidad", e);
                }
            }

            @Override
            public UnidadProducto guardar(UnidadProducto u) {
                String sql = "INSERT INTO unidad_producto (id_producto, id_ubicacion_actual, numero_serie, lote, "
                        + "vencimiento, estado_actual, fecha_alta) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, u.getProducto().getId());
                    setIdONulo(ps, 2, u.getUbicacionActual());
                    ps.setString(3, u.getNumeroSerie());
                    ps.setString(4, u.getLote());
                    ps.setDate(5, u.getVencimiento() == null ? null : Date.valueOf(u.getVencimiento()));
                    ps.setString(6, u.getEstado().name());
                    ps.setTimestamp(7, Timestamp.valueOf(u.getFechaAlta()));
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        u.setId(claves.getLong(1));
                    }
                    return u;
                } catch (SQLException e) {
                    throw new PersistenciaException("No se pudo guardar la unidad", e);
                }
            }

            @Override
            public void actualizar(UnidadProducto u) {
                String sql = "UPDATE unidad_producto SET estado_actual = ?, id_ubicacion_actual = ? WHERE id_unidad = ?";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setString(1, u.getEstado().name());
                    setIdONulo(ps, 2, u.getUbicacionActual());
                    ps.setLong(3, u.getId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new PersistenciaException("No se pudo actualizar la unidad", e);
                }
            }

            @Override
            public List<UnidadProducto> listarTodas() {
                List<UnidadProducto> lista = new ArrayList<>();
                try (Statement st = conexion.createStatement();
                     ResultSet rs = st.executeQuery(SELECT_UNIDAD + "ORDER BY u.id_unidad")) {
                    while (rs.next()) {
                        lista.add(mapearUnidad(rs));
                    }
                    return lista;
                } catch (SQLException e) {
                    throw new PersistenciaException("No se pudo listar las unidades", e);
                }
            }
        };
    }

    @Override
    public EventoDAO eventos() {
        return new EventoDAO() {
            private static final String SELECT_EVENTO = "SELECT e.*, te.codigo FROM evento e "
                    + "JOIN tipo_evento te ON te.id_tipo_evento = e.id_tipo_evento ";

            @Override
            public Evento guardar(Evento ev) {
                String sql = "INSERT INTO evento (id_unidad, id_tipo_evento, id_usuario, id_ubicacion_origen, "
                        + "id_ubicacion_destino, id_agente_origen, id_agente_destino, id_profesional, fecha_hora, documento, motivo, "
                        + "estado_envio) VALUES (?, (SELECT id_tipo_evento FROM tipo_evento WHERE codigo = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                // En la devolución el agente es el destino de la unidad; en la recepción, su origen.
                boolean agenteEsDestino = ev.getTipo() == TipoEvento.DEVOLUCION;
                try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, ev.getUnidad().getId());
                    ps.setString(2, ev.getTipo().name());
                    ps.setLong(3, ev.getUsuario().getId());
                    setIdONulo(ps, 4, ev.getUbicacionOrigen());
                    setIdONulo(ps, 5, ev.getUbicacionDestino());
                    setIdONulo(ps, 6, agenteEsDestino ? null : ev.getAgente());
                    setIdONulo(ps, 7, agenteEsDestino ? ev.getAgente() : null);
                    setIdONulo(ps, 8, ev.getProfesional());
                    ps.setTimestamp(9, Timestamp.valueOf(ev.getFechaHora()));
                    ps.setString(10, ev.getDocumento());
                    ps.setString(11, ev.getMotivo());
                    ps.setString(12, ev.getEstadoEnvio().name());
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        ev.setId(claves.getLong(1));
                    }
                    return ev;
                } catch (SQLException e) {
                    throw new PersistenciaException("No se pudo guardar el evento", e);
                }
            }

            private List<Evento> consultar(String condicion, Long parametro) {
                List<Evento> lista = new ArrayList<>();
                try (PreparedStatement ps = conexion.prepareStatement(SELECT_EVENTO + condicion)) {
                    if (parametro != null) {
                        ps.setLong(1, parametro);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            lista.add(mapearEvento(rs));
                        }
                    }
                    return lista;
                } catch (SQLException e) {
                    throw new PersistenciaException("No se pudo consultar eventos", e);
                }
            }

            private Evento mapearEvento(ResultSet rs) throws SQLException {
                UnidadProducto unidad = buscarUnidadPorId(rs.getLong("id_unidad")).orElseThrow();
                Long origen = leerId(rs, "id_ubicacion_origen");
                Long destino = leerId(rs, "id_ubicacion_destino");
                Long agente = leerId(rs, "id_agente_origen");
                if (agente == null) {
                    agente = leerId(rs, "id_agente_destino");
                }
                Long profesional = leerId(rs, "id_profesional");
                return new Evento(rs.getLong("id_evento"), unidad, TipoEvento.valueOf(rs.getString("codigo")),
                        usuarios.get(rs.getLong("id_usuario")),
                        origen == null ? null : ubicaciones.get(origen),
                        destino == null ? null : ubicaciones.get(destino),
                        agente == null ? null : agentes.get(agente),
                        profesional == null ? null : profesionales.get(profesional),
                        rs.getTimestamp("fecha_hora").toLocalDateTime(), rs.getString("documento"),
                        rs.getString("motivo"), EstadoEnvio.valueOf(rs.getString("estado_envio")));
            }

            @Override
            public List<Evento> listarPorUnidad(Long unidadId) {
                return consultar("WHERE e.id_unidad = ? ORDER BY e.fecha_hora, e.id_evento", unidadId);
            }

            @Override
            public List<Evento> listarPendientes() {
                return consultar("WHERE e.estado_envio = 'PENDIENTE' ORDER BY e.fecha_hora, e.id_evento", null);
            }

            @Override
            public void registrarIntentoEnvio(Evento ev, String codigoExterno) {
                String estadoTx;
                switch (ev.getEstadoEnvio()) {
                    case ACEPTADO:
                        estadoTx = "ACEPTADA";
                        break;
                    case RECHAZADO:
                        estadoTx = "RECHAZADA";
                        break;
                    default:
                        estadoTx = "ERROR";
                }
                try (PreparedStatement up = conexion.prepareStatement("UPDATE evento SET estado_envio = ? WHERE id_evento = ?");
                     PreparedStatement in = conexion.prepareStatement("INSERT INTO transaccion_externa "
                             + "(id_evento, codigo_externo, estado, respuesta, numero_intento) VALUES (?, ?, ?, ?, "
                             + "(SELECT COALESCE(MAX(t.numero_intento), 0) + 1 FROM transaccion_externa t WHERE t.id_evento = ?))")) {
                    up.setString(1, ev.getEstadoEnvio().name());
                    up.setLong(2, ev.getId());
                    up.executeUpdate();
                    in.setLong(1, ev.getId());
                    in.setString(2, codigoExterno);
                    in.setString(3, estadoTx);
                    in.setString(4, ev.getRespuestaExterna());
                    in.setLong(5, ev.getId());
                    in.executeUpdate();
                } catch (SQLException e) {
                    throw new PersistenciaException("No se pudo registrar el intento de envío", e);
                }
            }
        };
    }

    @Override
    public CatalogoDAO catalogo() {
        return new CatalogoDAO() {
            @Override
            public List<Ubicacion> listarUbicaciones() {
                return new ArrayList<>(ubicaciones.values());
            }

            @Override
            public List<Agente> listarAgentes() {
                return new ArrayList<>(agentes.values());
            }

            @Override
            public List<Profesional> listarProfesionales() {
                return new ArrayList<>(profesionales.values());
            }

            @Override
            public List<Usuario> listarUsuarios() {
                return new ArrayList<>(usuarios.values());
            }
        };
    }

    /** Confirma el bloque con commit o lo revierte completo con rollback. */
    @Override
    public <T> T enTransaccion(Transaccion<T> trabajo) throws TrazabilidadException {
        try {
            conexion.setAutoCommit(false);
            T resultado = trabajo.ejecutar();
            conexion.commit();
            return resultado;
        } catch (TrazabilidadException | RuntimeException e) {
            deshacer();
            throw e;
        } catch (SQLException e) {
            deshacer();
            throw new PersistenciaException("Error al confirmar la transacción", e);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ignorada) {
                // la conexión pudo haberse cerrado; el próximo uso informará el error
            }
        }
    }

    private void deshacer() {
        try {
            conexion.rollback();
        } catch (SQLException ignorada) {
            // si falla el rollback, MySQL descarta la transacción al cerrar la conexión
        }
    }

    @Override
    public String getDescripcion() {
        try {
            return "MySQL: " + conexion.getMetaData().getURL();
        } catch (SQLException e) {
            return "MySQL";
        }
    }
}
