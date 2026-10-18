package com.trazapm.vista;

import com.trazapm.config.Contexto;
import com.trazapm.control.SolicitudMovimiento;
import com.trazapm.control.operaciones.OperacionTrazabilidad;
import com.trazapm.excepciones.TrazabilidadException;
import com.trazapm.modelo.Agente;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.Profesional;
import com.trazapm.modelo.Ubicacion;
import com.trazapm.modelo.UnidadProducto;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Pantalla de movimientos (traslado, cuarentena, liberación, implantación y
 * devolución). El combo contiene objetos OperacionTrazabilidad: al elegir uno,
 * la propia operación indica qué campos usa, y al confirmar el servicio la
 * ejecuta sin saber de qué subclase se trata.
 */
public class PantallaMovimientos extends PantallaBase {

    private final TextField txtGtin = new TextField();
    private final TextField txtSerie = new TextField();
    private final Label lblUnidad = new Label("Busque la unidad por GTIN y número de serie.");
    private final Label lblDetalle = new Label();
    private final Label lblEstado = new Label();
    private final Label lblUbicacion = new Label();
    private final ComboBox<OperacionTrazabilidad> cbOperacion = new ComboBox<>();
    private final ComboBox<Ubicacion> cbDestino = new ComboBox<>();
    private final ComboBox<Agente> cbAgente = new ComboBox<>();
    private final ComboBox<Profesional> cbProfesional = new ComboBox<>();
    private final TextField txtDocumento = new TextField();
    private final TextField txtMotivo = new TextField();
    private final Label lblMensaje = new Label("Seleccione la unidad y el evento a registrar.");
    private final Runnable alRegistrar;
    private UnidadProducto unidad;

    public PantallaMovimientos(Contexto contexto, Runnable alRegistrar) {
        super(contexto, "Registrar movimiento", "CU-02, CU-03, CU-04 y CU-09 · Las reglas de cada evento se validan antes de confirmar.");
        this.alRegistrar = alRegistrar;
        txtGtin.setId("txtGtinMov");
        txtSerie.setId("txtSerieMov");
        cbOperacion.setId("cbOperacion");
        cbDestino.setId("cbDestinoMov");
        cbAgente.setId("cbAgenteMov");
        cbProfesional.setId("cbProfesionalMov");
        txtDocumento.setId("txtDocumentoMov");
        txtMotivo.setId("txtMotivoMov");
        lblMensaje.setId("lblMensajeMov");

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setId("btnBuscarMov");
        btnBuscar.getStyleClass().add("boton-primario");
        btnBuscar.setOnAction(e -> buscar());
        GridPane criterios = grilla(3);
        campo(criterios, "GTIN", txtGtin, 0, 0);
        campo(criterios, "Número de serie", txtSerie, 1, 0);
        VBox cajaBoton = new VBox(btnBuscar);
        cajaBoton.setAlignment(Pos.BOTTOM_LEFT);
        criterios.add(cajaBoton, 2, 0);

        lblUnidad.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label tituloUbic = new Label("Ubicación actual:");
        lblUbicacion.setStyle("-fx-font-weight: bold;");
        lblEstado.setId("lblEstadoMov");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox tarjetaUnidad = new HBox(30, new VBox(4, lblUnidad, lblDetalle), espacio,
                new VBox(4, tituloUbic, lblUbicacion), lblEstado);
        tarjetaUnidad.setAlignment(Pos.CENTER_LEFT);

        for (ComboBox<?> c : new ComboBox<?>[]{cbOperacion, cbDestino, cbAgente, cbProfesional}) {
            c.setMaxWidth(Double.MAX_VALUE);
        }
        cbOperacion.valueProperty().addListener((obs, ant, op) -> habilitarCampos(op));
        GridPane evento = grilla(3);
        campo(evento, "Evento *", cbOperacion, 0, 0);
        campo(evento, "Ubicación de destino", cbDestino, 1, 0);
        campo(evento, "Agente (devolución)", cbAgente, 2, 0);
        campo(evento, "Profesional (implantación)", cbProfesional, 0, 1);
        campo(evento, "Documento / parte quirúrgico", txtDocumento, 1, 1);
        campo(evento, "Motivo / observaciones", txtMotivo, 2, 1);

        Button btnRegistrar = new Button("Registrar evento");
        btnRegistrar.setId("btnRegistrarMov");
        btnRegistrar.getStyleClass().add("boton-primario");
        btnRegistrar.setOnAction(e -> registrar());
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(Double.MAX_VALUE);
        mostrar(lblMensaje, lblMensaje.getText(), "mensaje-info");
        HBox.setHgrow(lblMensaje, Priority.ALWAYS);
        HBox acciones = new HBox(14, lblMensaje, btnRegistrar);
        acciones.setAlignment(Pos.CENTER_LEFT);

        contenido.getChildren().addAll(seccion("Criterios de búsqueda", criterios),
                seccion("Unidad seleccionada", tarjetaUnidad), seccion("Evento a registrar", evento), acciones);
        habilitarCampos(null);
    }

    private void habilitarCampos(OperacionTrazabilidad op) {
        cbDestino.setDisable(op == null || !op.usaDestino());
        cbAgente.setDisable(op == null || !op.usaAgente());
        cbProfesional.setDisable(op == null || !op.usaProfesional());
        if (op != null && !op.usaDestino()) {
            cbDestino.setValue(null);
        }
    }

    private void buscar() {
        try {
            unidad = contexto.getServicioTrazabilidad().consultarHistorial(txtGtin.getText(), txtSerie.getText()).getUnidad();
            mostrarUnidad();
            mostrar(lblMensaje, "Unidad encontrada. Elija el evento a registrar.", "mensaje-info");
        } catch (TrazabilidadException | RuntimeException e) {
            unidad = null;
            mostrarUnidad();
            mostrarError(lblMensaje, e);
        }
    }

    private void mostrarUnidad() {
        lblEstado.getStyleClass().removeIf(c -> c.startsWith("estado"));
        if (unidad == null) {
            lblUnidad.setText("Sin unidad seleccionada");
            lblDetalle.setText("");
            lblUbicacion.setText("-");
            lblEstado.setText("");
            return;
        }
        lblUnidad.setText(unidad.getProducto().getDescripcion());
        lblDetalle.setText("Serie " + unidad.getNumeroSerie() + " · Lote " + unidad.getLote()
                + " · Vence " + formatear(unidad.getVencimiento()));
        lblUbicacion.setText(unidad.getUbicacionActual() == null ? "Fuera del establecimiento"
                : unidad.getUbicacionActual().getNombre());
        lblEstado.setText(unidad.getEstado().name());
        lblEstado.getStyleClass().addAll("estado", "estado-" + unidad.getEstado().name());
    }

    private void registrar() {
        try {
            if (unidad == null) {
                throw new TrazabilidadException("Primero busque la unidad.");
            }
            OperacionTrazabilidad op = cbOperacion.getValue();
            if (op == null) {
                throw new TrazabilidadException("Seleccione el evento a registrar.");
            }
            SolicitudMovimiento s = new SolicitudMovimiento(unidad, contexto.getUsuarioActual(), cbDestino.getValue(),
                    cbAgente.getValue(), cbProfesional.getValue(), txtDocumento.getText(), txtMotivo.getText());
            Evento ev = contexto.getServicioTrazabilidad().registrarMovimiento(op, s);
            mostrarUnidad();
            mostrar(lblMensaje, "✔ Evento \"" + op.getTipoEvento().getNombre() + "\" registrado con el número "
                    + ev.getId() + ". Estado actual de la unidad: " + unidad.getEstado().getEtiqueta() + ".", "mensaje-ok");
            txtDocumento.clear();
            txtMotivo.clear();
            alRegistrar.run();
        } catch (TrazabilidadException | RuntimeException e) {
            mostrarError(lblMensaje, e);
        }
    }

    @Override
    public void refrescar() {
        cbOperacion.getItems().setAll(contexto.getServicioTrazabilidad().getOperaciones());
        cbDestino.getItems().setAll(contexto.getDatos().catalogo().listarUbicaciones());
        cbAgente.getItems().setAll(contexto.getDatos().catalogo().listarAgentes());
        cbProfesional.getItems().setAll(contexto.getDatos().catalogo().listarProfesionales());
        mostrarUnidad();
    }

    @Override
    public String getTituloPestana() {
        return "Registrar movimiento";
    }
}
