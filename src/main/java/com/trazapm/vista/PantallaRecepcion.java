package com.trazapm.vista;

import com.trazapm.config.Contexto;
import com.trazapm.control.DatosRecepcion;
import com.trazapm.control.ResultadoRecepcion;
import com.trazapm.excepciones.TrazabilidadException;
import com.trazapm.modelo.Agente;
import com.trazapm.modelo.Producto;
import com.trazapm.modelo.Ubicacion;
import com.trazapm.util.ValidadorGtin;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Pantalla del CU-01 "Registrar recepción" (ver Figura 9 del TP2).
 * Solo arma el formulario y delega la regla de negocio en ControlRecepcion.
 */
public class PantallaRecepcion extends PantallaBase {

    private final TextField txtGtin = new TextField();
    private final TextField txtSerie = new TextField();
    private final TextField txtProducto = new TextField();
    private final TextField txtLote = new TextField();
    private final TextField txtVencimiento = new TextField();
    private final ComboBox<Agente> cbProveedor = new ComboBox<>();
    private final TextField txtDocumento = new TextField();
    private final TextField txtFecha = new TextField();
    private final ComboBox<Ubicacion> cbDestino = new ComboBox<>();
    private final Label lblMensaje = new Label("La combinación GTIN y número de serie se validará antes de confirmar.");
    private final Runnable alRegistrar;

    public PantallaRecepcion(Contexto contexto, Runnable alRegistrar) {
        super(contexto, "Registrar recepción de producto médico", "CU-01 · Los campos marcados con * son obligatorios.");
        this.alRegistrar = alRegistrar;
        txtGtin.setId("txtGtin");
        txtSerie.setId("txtSerie");
        txtLote.setId("txtLote");
        txtVencimiento.setId("txtVencimiento");
        cbProveedor.setId("cbProveedor");
        txtDocumento.setId("txtDocumento");
        lblMensaje.setId("lblMensajeRecepcion");

        txtVencimiento.setPromptText("dd/mm/aaaa");
        txtProducto.setEditable(false);
        txtProducto.setFocusTraversable(false);
        txtFecha.setEditable(false);
        cbDestino.setDisable(true);
        cbProveedor.setMaxWidth(Double.MAX_VALUE);
        cbDestino.setMaxWidth(Double.MAX_VALUE);
        // Al escribir el GTIN se muestra el producto del catálogo o el motivo por el cual no se reconoce.
        txtGtin.textProperty().addListener((obs, anterior, nuevo) -> mostrarProducto(nuevo));

        GridPane identificacion = grilla(3);
        campo(identificacion, "GTIN *", txtGtin, 0, 0);
        campo(identificacion, "Número de serie *", txtSerie, 1, 0);
        campo(identificacion, "Producto", txtProducto, 2, 0);
        campo(identificacion, "Lote *", txtLote, 0, 1);
        campo(identificacion, "Vencimiento *", txtVencimiento, 1, 1);

        GridPane recepcion = grilla(3);
        campo(recepcion, "Agente de origen *", cbProveedor, 0, 0);
        campo(recepcion, "Documento (remito / factura) *", txtDocumento, 1, 0);
        campo(recepcion, "Fecha y hora", txtFecha, 2, 0);
        campo(recepcion, "Ubicación de destino", cbDestino, 0, 1);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("boton-secundario");
        btnCancelar.setOnAction(e -> limpiar());
        Button btnConfirmar = new Button("Confirmar");
        btnConfirmar.setId("btnConfirmarRecepcion");
        btnConfirmar.getStyleClass().add("boton-primario");
        btnConfirmar.setDefaultButton(true);
        btnConfirmar.setOnAction(e -> confirmar());

        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(Double.MAX_VALUE);
        mostrar(lblMensaje, lblMensaje.getText(), "mensaje-info");
        HBox.setHgrow(lblMensaje, Priority.ALWAYS);
        HBox acciones = new HBox(14, lblMensaje, btnCancelar, btnConfirmar);
        acciones.setAlignment(Pos.CENTER_LEFT);

        contenido.getChildren().addAll(seccion("Identificación de la unidad", identificacion),
                seccion("Datos de la recepción", recepcion), acciones);
    }

    private void mostrarProducto(String gtin) {
        String valor = gtin == null ? "" : gtin.trim();
        if (valor.length() < 14) {
            txtProducto.setText("");
        } else if (!ValidadorGtin.esValido(valor)) {
            txtProducto.setText("GTIN inválido (dígito verificador)");
        } else {
            Optional<Producto> p = contexto.getDatos().productos().buscarPorGtin(valor);
            txtProducto.setText(p.map(x -> x.getDescripcion() + " · " + x.getDetalleTecnico())
                    .orElse("No figura en el catálogo"));
        }
    }

    private void confirmar() {
        try {
            LocalDate vencimiento = leerFecha(txtVencimiento.getText(), "vencimiento");
            DatosRecepcion datos = new DatosRecepcion(txtGtin.getText(), txtSerie.getText(), txtLote.getText(),
                    vencimiento, cbProveedor.getValue(), txtDocumento.getText());
            ResultadoRecepcion r = contexto.getControlRecepcion().registrarRecepcion(datos, contexto.getUsuarioActual());
            mostrar(lblMensaje, "✔ " + r.getMensaje(), "mensaje-ok");
            limpiarCampos();
            alRegistrar.run();
        } catch (TrazabilidadException | RuntimeException e) {
            mostrarError(lblMensaje, e);
        }
    }

    private void limpiarCampos() {
        txtGtin.clear();
        txtSerie.clear();
        txtLote.clear();
        txtVencimiento.clear();
        txtDocumento.clear();
        txtFecha.setText(formatear(LocalDateTime.now()));
    }

    private void limpiar() {
        limpiarCampos();
        mostrar(lblMensaje, "La combinación GTIN y número de serie se validará antes de confirmar.", "mensaje-info");
    }

    @Override
    public void refrescar() {
        cbProveedor.getItems().setAll(contexto.getDatos().catalogo().listarAgentes());
        cbDestino.getItems().setAll(contexto.getDatos().catalogo().listarUbicaciones());
        if (!cbDestino.getItems().isEmpty()) {
            cbDestino.getSelectionModel().select(0);
        }
        txtFecha.setText(formatear(LocalDateTime.now()));
    }

    @Override
    public String getTituloPestana() {
        return "Registrar recepción";
    }
}
