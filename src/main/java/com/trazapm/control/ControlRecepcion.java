package com.trazapm.control;

import com.trazapm.dao.FuenteDatos;
import com.trazapm.excepciones.DatosInvalidosException;
import com.trazapm.excepciones.PermisoDenegadoException;
import com.trazapm.excepciones.TrazabilidadException;
import com.trazapm.excepciones.UnidadDuplicadaException;
import com.trazapm.modelo.EstadoEnvio;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.Producto;
import com.trazapm.modelo.Rol;
import com.trazapm.modelo.TipoEvento;
import com.trazapm.modelo.Ubicacion;
import com.trazapm.modelo.UnidadProducto;
import com.trazapm.modelo.Usuario;
import com.trazapm.util.ValidadorGtin;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clase de control del CU-01 "Registrar recepción". Sigue la secuencia de
 * diseño: validar datos, verificar unicidad GTIN-serie, crear la unidad en
 * el depósito, registrar el evento y encolarlo para el SNT-PM.
 */
public class ControlRecepcion {

    private static final String UBICACION_INGRESO = "Depósito central";

    private final FuenteDatos datos;
    private final ServicioIntegracion integracion;

    public ControlRecepcion(FuenteDatos datos, ServicioIntegracion integracion) {
        this.datos = datos;
        this.integracion = integracion;
    }

    public ResultadoRecepcion registrarRecepcion(DatosRecepcion d, Usuario usuario) throws TrazabilidadException {
        if (usuario.getRol() == Rol.PROFESIONAL) {
            throw new PermisoDenegadoException("El rol Profesional no registra recepciones.");
        }
        Producto producto = validar(d);

        Optional<UnidadProducto> existente = datos.unidades().buscarPorGtinYSerie(d.getGtin(), d.getNumeroSerie());
        if (existente.isPresent()) {
            throw new UnidadDuplicadaException(existente.get());
        }

        Ubicacion deposito = buscarDeposito();
        // Alta de unidad y evento en una sola transacción: o se guardan ambos o ninguno.
        Evento evento = datos.enTransaccion(() -> {
            UnidadProducto unidad = datos.unidades().guardar(
                    new UnidadProducto(producto, d.getNumeroSerie(), d.getLote(), d.getVencimiento(), deposito));
            return datos.eventos().guardar(new Evento(null, unidad, TipoEvento.RECEPCION, usuario, null, deposito,
                    d.getProveedor(), null, LocalDateTime.now(), d.getDocumento(), "Recepción de mercadería",
                    EstadoEnvio.PENDIENTE));
        });
        integracion.encolar(evento);
        return new ResultadoRecepcion(evento.getUnidad(), evento,
                "Unidad registrada en " + deposito + " como Disponible. Evento #" + evento.getId()
                        + " pendiente de informar al SNT-PM.");
    }

    /** Valida todos los campos y acumula los errores para mostrarlos juntos. */
    private Producto validar(DatosRecepcion d) throws DatosInvalidosException {
        List<String> errores = new ArrayList<>();
        Producto producto = null;
        if (!ValidadorGtin.esValido(d.getGtin())) {
            errores.add("El GTIN debe tener 14 dígitos y un dígito verificador válido.");
        } else {
            producto = datos.productos().buscarPorGtin(d.getGtin()).orElse(null);
            if (producto == null) {
                errores.add("El GTIN " + d.getGtin() + " no corresponde a un producto del catálogo.");
            }
        }
        if (d.getNumeroSerie().isEmpty()) {
            errores.add("El número de serie es obligatorio.");
        } else if (d.getNumeroSerie().length() > 80) {
            errores.add("El número de serie no puede superar 80 caracteres.");
        }
        if (d.getLote().isEmpty()) {
            errores.add("El lote es obligatorio.");
        }
        if (d.getVencimiento() == null) {
            errores.add("La fecha de vencimiento es obligatoria.");
        } else if (!d.getVencimiento().isAfter(LocalDate.now())) {
            errores.add("No se pueden recibir unidades vencidas o que vencen hoy.");
        }
        if (d.getProveedor() == null) {
            errores.add("Debe seleccionar el agente proveedor.");
        }
        if (d.getDocumento().isEmpty()) {
            errores.add("Debe indicar el remito o factura de la recepción.");
        }
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
        return producto;
    }

    private Ubicacion buscarDeposito() throws TrazabilidadException {
        for (Ubicacion u : datos.catalogo().listarUbicaciones()) {
            if (u.getNombre().equalsIgnoreCase(UBICACION_INGRESO)) {
                return u;
            }
        }
        throw new TrazabilidadException("No está configurada la ubicación \"" + UBICACION_INGRESO + "\".");
    }
}
