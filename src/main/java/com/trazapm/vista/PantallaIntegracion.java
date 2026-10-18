package com.trazapm.vista;

import com.trazapm.config.Contexto;
import com.trazapm.control.ResumenSincronizacion;
import com.trazapm.modelo.Evento;
import java.time.LocalDateTime;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Pantalla del CU-07 "Sincronizar eventos pendientes" (ver Figura 11 del TP2). */
public class PantallaIntegracion extends PantallaBase {

    private final Label lblPendientes = new Label("0");
    private final Label lblAceptados = new Label("0");
    private final Label lblRechazados = new Label("0");
    private final CheckBox chkDisponible = new CheckBox("Servicio SNT-PM disponible (simulado)");
    private final TableView<Evento> tabla = new TableView<>();
    private final TextArea txtRegistro = new TextArea("Esperando una operación. Presione Procesar para informar la cola.");
    private final Runnable alSincronizar;
    private int aceptadosSesion;
    private int rechazadosSesion;

    public PantallaIntegracion(Contexto contexto, Runnable alSincronizar) {
        super(contexto, "Cola de integración con SNT-PM simulado",
                "CU-07 · Los eventos locales se conservan aunque el servicio externo no responda.");
        this.alSincronizar = alSincronizar;
        chkDisponible.setId("chkDisponible");
        chkDisponible.setSelected(true);
        chkDisponible.setOnAction(e -> {
            contexto.getConector().setDisponible(chkDisponible.isSelected());
            alSincronizar.run();
        });
        Button btnActualizar = new Button("Actualizar");
        btnActualizar.getStyleClass().add("boton-secundario");
        btnActualizar.setOnAction(e -> refrescar());
        Button btnProcesar = new Button("Procesar");
        btnProcesar.setId("btnProcesar");
        btnProcesar.getStyleClass().add("boton-primario");
        btnProcesar.setOnAction(e -> procesar());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox resumen = new HBox(18, tarjeta("Pendientes", lblPendientes, "tarjeta-amarilla"),
                tarjeta("Aceptados (sesión)", lblAceptados, "tarjeta-verde"),
                tarjeta("Rechazados (sesión)", lblRechazados, "tarjeta-roja"), espacio, btnActualizar, btnProcesar);
        resumen.setAlignment(Pos.CENTER_LEFT);

        tabla.getColumns().add(PantallaTrazabilidad.columna("Evento", (Evento ev) -> ev.getTipo().getNombre(), 160));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Unidad", (Evento ev) -> ev.getUnidad().getProducto().getGtin()
                + " / " + ev.getUnidad().getNumeroSerie(), 230));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Fecha", (Evento ev) -> formatear(ev.getFechaHora()), 130));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Intentos", (Evento ev) -> String.valueOf(ev.getIntentosEnvio()), 70));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Último resultado", (Evento ev) ->
                ev.getRespuestaExterna() == null ? "Pendiente de primer envío" : ev.getRespuestaExterna(), 300));
        // La última columna absorbe el espacio libre y conserva legibles GTIN, serie y respuesta.
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPrefHeight(220);
        tabla.setPlaceholder(new Label("No hay eventos pendientes"));

        txtRegistro.setId("txtRegistro");
        txtRegistro.setEditable(false);
        txtRegistro.setPrefRowCount(5);
        txtRegistro.getStyleClass().add("registro");
        contenido.getChildren().addAll(seccion("Resumen", resumen),
                seccion("Eventos pendientes", new VBox(10, tabla, chkDisponible)),
                seccion("Registro de ejecución", txtRegistro));
    }

    private VBox tarjeta(String titulo, Label valor, String estilo) {
        valor.getStyleClass().add("tarjeta-valor");
        VBox t = new VBox(2, new Label(titulo), valor);
        t.getStyleClass().addAll("tarjeta", estilo);
        return t;
    }

    private void procesar() {
        int antes = contexto.getIntegracion().cantidadPendientes();
        try {
            ResumenSincronizacion r = contexto.getIntegracion().sincronizar();
            aceptadosSesion += r.getAceptados();
            rechazadosSesion += r.getRechazados();
            txtRegistro.appendText("\n[" + formatear(LocalDateTime.now()) + "] Procesados " + antes + " eventos. " + r);
            if (r.getFallidos() > 0) {
                txtRegistro.appendText("\n    El servicio no respondió: los eventos siguen en la cola y se reintentarán.");
            }
        } catch (RuntimeException e) {
            txtRegistro.appendText("\n[ERROR] " + e.getMessage());
        }
        refrescar();
        alSincronizar.run();
    }

    @Override
    public void refrescar() {
        tabla.getItems().setAll(contexto.getIntegracion().getPendientes());
        lblPendientes.setText(String.valueOf(contexto.getIntegracion().cantidadPendientes()));
        lblAceptados.setText(String.valueOf(aceptadosSesion));
        lblRechazados.setText(String.valueOf(rechazadosSesion));
        chkDisponible.setSelected(contexto.getConector().isDisponible());
    }

    @Override
    public String getTituloPestana() {
        return "Cola de integración";
    }
}
