package com.trazapm.vista;

import com.trazapm.config.Contexto;
import com.trazapm.control.Historial;
import com.trazapm.control.exportacion.Exportador;
import java.nio.file.Path;
import java.util.Optional;
import com.trazapm.excepciones.TrazabilidadException;
import com.trazapm.modelo.Evento;
import com.trazapm.modelo.UnidadProducto;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Pantalla del CU-05 "Consultar trazabilidad" (ver Figura 10 del TP2). */
public class PantallaTrazabilidad extends PantallaBase {

    private final TextField txtGtin = new TextField();
    private final TextField txtSerie = new TextField();
    private final Label lblProducto = new Label("Sin unidad seleccionada");
    private final Label lblDetalle = new Label();
    private final Label lblUbicacion = new Label("-");
    private final Label lblEstado = new Label();
    private final TableView<Evento> tabla = new TableView<>();
    private final Label lblMensaje = new Label("Ingrese GTIN y número de serie y presione Buscar.");
    private final Button btnExportar = new Button("Exportar CSV");
    private Historial ultimoHistorial;

    public PantallaTrazabilidad(Contexto contexto) {
        super(contexto, "Consultar trazabilidad", "CU-05 · Historial cronológico y auditable de una unidad.");
        txtGtin.setId("txtGtinTraz");
        txtSerie.setId("txtSerieTraz");
        lblEstado.setId("lblEstadoTraz");

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setId("btnBuscarTraz");
        btnBuscar.getStyleClass().add("boton-primario");
        btnBuscar.setOnAction(e -> buscar());
        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.getStyleClass().add("boton-secundario");
        btnLimpiar.setOnAction(e -> limpiar());
        GridPane criterios = grilla(3);
        campo(criterios, "GTIN", txtGtin, 0, 0);
        campo(criterios, "Número de serie", txtSerie, 1, 0);
        btnExportar.setId("btnExportarTraz");
        btnExportar.getStyleClass().add("boton-secundario");
        btnExportar.setDisable(true);
        btnExportar.setOnAction(e -> exportar());
        HBox botones = new HBox(12, btnBuscar, btnLimpiar, btnExportar);
        botones.setAlignment(Pos.BOTTOM_LEFT);
        criterios.add(botones, 2, 0);

        lblProducto.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        lblUbicacion.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox tarjeta = new HBox(30, new VBox(4, lblProducto, lblDetalle), espacio,
                new VBox(4, new Label("Ubicación actual:"), lblUbicacion), lblEstado);
        tarjeta.setAlignment(Pos.CENTER_LEFT);

        tabla.getColumns().add(columna("Fecha y hora", ev -> formatear(ev.getFechaHora()), 138));
        tabla.getColumns().add(columna("Evento", ev -> ev.getTipo().getNombre(), 175));
        tabla.getColumns().add(columna("Origen", Historial::origenDe, 240));
        tabla.getColumns().add(columna("Destino", Historial::destinoDe, 250));
        tabla.getColumns().add(columna("Documento", Evento::getDocumento, 110));
        tabla.getColumns().add(columna("Envío", ev -> ev.getEstadoEnvio().name(), 95));
        tabla.getColumns().add(columna("Usuario", ev -> ev.getUsuario().getNombreUsuario(), 125));
        tabla.setPrefHeight(250);
        tabla.setPlaceholder(new Label("Sin eventos para mostrar"));

        lblMensaje.setMaxWidth(Double.MAX_VALUE);
        lblMensaje.setId("lblMensajeTraz");
        mostrar(lblMensaje, lblMensaje.getText(), "mensaje-info");
        contenido.getChildren().addAll(seccion("Criterios de búsqueda", criterios),
                seccion("Unidad encontrada", tarjeta), seccion("Historial de eventos", tabla), lblMensaje);
    }

    static <T> TableColumn<T, String> columna(String titulo, Function<T, String> valor, double ancho) {
        TableColumn<T, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(d -> new SimpleStringProperty(valor.apply(d.getValue())));
        c.setPrefWidth(ancho);
        return c;
    }

    private void buscar() {
        try {
            Historial h = contexto.getServicioTrazabilidad().consultarHistorial(txtGtin.getText(), txtSerie.getText());
            UnidadProducto u = h.getUnidad();
            lblProducto.setText(u.getProducto().getDescripcion());
            lblDetalle.setText("Serie " + u.getNumeroSerie() + " · Lote " + u.getLote() + " · Vence "
                    + formatear(u.getVencimiento()) + " · " + u.getProducto().getDetalleTecnico());
            lblUbicacion.setText(u.getUbicacionActual() == null ? "Fuera del establecimiento" : u.getUbicacionActual().getNombre());
            lblEstado.getStyleClass().removeIf(c -> c.startsWith("estado"));
            lblEstado.getStyleClass().addAll("estado", "estado-" + u.getEstado().name());
            lblEstado.setText(u.getEstado().name());
            tabla.getItems().setAll(h.getEventos());
            ultimoHistorial = h;
            btnExportar.setDisable(false);
            mostrar(lblMensaje, h.getEventos().size() + " eventos recuperados.", "mensaje-info");
        } catch (TrazabilidadException | RuntimeException e) {
            tabla.getItems().clear();
            ultimoHistorial = null;
            btnExportar.setDisable(true);
            mostrarError(lblMensaje, e);
        }
    }

    /** Exporta el historial consultado a un archivo (CU-05, paso 3). */
    private void exportar() {
        if (ultimoHistorial == null) {
            return;
        }
        Exportador exportador = contexto.getExportador();
        UnidadProducto u = ultimoHistorial.getUnidad();
        Optional<Path> destino = elegirArchivoDestino("historial_" + u.getProducto().getGtin() + "_"
                + u.getNumeroSerie(), exportador.getExtension());
        if (destino.isEmpty()) {
            return;
        }
        try {
            exportador.exportarHistorial(ultimoHistorial, destino.get());
            mostrar(lblMensaje, "Historial exportado en " + destino.get(), "mensaje-ok");
        } catch (TrazabilidadException e) {
            mostrarError(lblMensaje, e);
        }
    }

    private void limpiar() {
        ultimoHistorial = null;
        btnExportar.setDisable(true);
        txtGtin.clear();
        txtSerie.clear();
        tabla.getItems().clear();
        lblProducto.setText("Sin unidad seleccionada");
        lblDetalle.setText("");
        lblUbicacion.setText("-");
        lblEstado.setText("");
        lblEstado.getStyleClass().removeIf(c -> c.startsWith("estado"));
    }

    /** Permite abrir la consulta ya cargada desde otras pantallas. */
    public void consultar(String gtin, String serie) {
        txtGtin.setText(gtin);
        txtSerie.setText(serie);
        buscar();
    }

    @Override
    public void refrescar() {
        if (!tabla.getItems().isEmpty()) {
            buscar();
        }
    }

    @Override
    public String getTituloPestana() {
        return "Consultar trazabilidad";
    }
}
