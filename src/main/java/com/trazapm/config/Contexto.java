package com.trazapm.config;

import com.trazapm.control.ConectorSntSimulado;
import com.trazapm.control.ControlRecepcion;
import com.trazapm.control.ServicioIntegracion;
import com.trazapm.control.ServicioTrazabilidad;
import com.trazapm.control.exportacion.Exportador;
import com.trazapm.control.exportacion.ExportadorCsv;
import com.trazapm.dao.FuenteDatos;
import com.trazapm.dao.jdbc.ConexionMySQL;
import com.trazapm.dao.jdbc.FuenteDatosMySQL;
import com.trazapm.dao.memoria.FuenteDatosMemoria;
import com.trazapm.modelo.Usuario;
import java.sql.SQLException;

/**
 * Arma los objetos de la aplicación al iniciar. Intenta conectar con MySQL
 * y, si no es posible, continúa en modo demostración con datos en memoria,
 * para que el prototipo siempre pueda ejecutarse.
 */
public class Contexto {

    private final FuenteDatos datos;
    private final String avisoConexion;
    private final ConectorSntSimulado conector = new ConectorSntSimulado();
    private final ServicioIntegracion integracion;
    private final ControlRecepcion controlRecepcion;
    private final ServicioTrazabilidad servicioTrazabilidad;
    private final Exportador exportador = new ExportadorCsv();
    private Usuario usuarioActual;

    public Contexto(boolean forzarMemoria) {
        FuenteDatos fuente;
        String aviso = null;
        if (forzarMemoria) {
            fuente = new FuenteDatosMemoria();
        } else {
            try {
                fuente = new FuenteDatosMySQL(ConexionMySQL.abrir());
            } catch (SQLException e) {
                fuente = new FuenteDatosMemoria();
                aviso = "No se pudo conectar con MySQL (" + e.getMessage()
                        + "). Se continúa en modo demostración con datos en memoria.";
            }
        }
        this.datos = fuente;
        this.avisoConexion = aviso;
        this.integracion = new ServicioIntegracion(datos.eventos(), conector);
        this.controlRecepcion = new ControlRecepcion(datos, integracion);
        this.servicioTrazabilidad = new ServicioTrazabilidad(datos, integracion);
        this.usuarioActual = datos.catalogo().listarUsuarios().get(0);
    }

    public FuenteDatos getDatos() {
        return datos;
    }

    public String getAvisoConexion() {
        return avisoConexion;
    }

    public ConectorSntSimulado getConector() {
        return conector;
    }

    public ServicioIntegracion getIntegracion() {
        return integracion;
    }

    public ControlRecepcion getControlRecepcion() {
        return controlRecepcion;
    }

    public ServicioTrazabilidad getServicioTrazabilidad() {
        return servicioTrazabilidad;
    }

    public Exportador getExportador() {
        return exportador;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }
}
