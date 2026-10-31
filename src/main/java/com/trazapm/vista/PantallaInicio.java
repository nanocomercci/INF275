package com.trazapm.vista;

import com.trazapm.config.Contexto;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.UnidadProducto;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Pantalla inicial con indicadores y accesos rápidos (menú de selección). */
public class PantallaInicio extends PantallaBase {

    private final FlowPane indicadores = new FlowPane(14, 14);
    private final VBox alertas = new VBox(6);
    private final Label lblBienvenida = new Label();

    public PantallaInicio(Contexto contexto, Consumer<Integer> irAPestana) {
        super(contexto, "TrazaPM · Clínica Modelo del Centro",
                "Sistema de gestión y trazabilidad de productos médicos implantables (prototipo TP4 - proyecto integrador).");
        lblBienvenida.setStyle("-fx-font-size: 14px;");
        String[] accesos = {"Registrar recepción", "Registrar movimiento", "Consultar trazabilidad",
            "Stock y vencimientos", "Cola de integración"};
        HBox botones = new HBox(10);
        for (int i = 0; i < accesos.length; i++) {
            final int pestana = i + 1;
            Button b = new Button(accesos[i]);
            b.getStyleClass().add(i == 0 ? "boton-primario" : "boton-secundario");
            b.setOnAction(e -> irAPestana.accept(pestana));
            botones.getChildren().add(b);
        }
        botones.setAlignment(Pos.CENTER_LEFT);
        contenido.getChildren().addAll(lblBienvenida, seccion("¿Qué desea hacer?", botones),
                seccion("Indicadores", indicadores), seccion("Alertas de vencimiento", alertas));
    }

    private VBox tarjeta(String titulo, int valor, String estilo) {
        Label v = new Label(String.valueOf(valor));
        v.getStyleClass().add("tarjeta-valor");
        VBox t = new VBox(2, new Label(titulo), v);
        t.getStyleClass().addAll("tarjeta", estilo);
        return t;
    }

    @Override
    public void refrescar() {
        lblBienvenida.setText("Usuario activo: " + contexto.getUsuarioActual().getNombreCompleto() + " · "
                + contexto.getUsuarioActual().getDescripcionRol());
        Map<EstadoUnidad, Integer> conteo = contexto.getServicioTrazabilidad().contarPorEstado();
        indicadores.getChildren().setAll(
                tarjeta("Disponibles", conteo.get(EstadoUnidad.DISPONIBLE), "tarjeta-verde"),
                tarjeta("En cuarentena", conteo.get(EstadoUnidad.CUARENTENA), "tarjeta-amarilla"),
                tarjeta("Implantadas", conteo.get(EstadoUnidad.IMPLANTADA), "tarjeta-azul"),
                tarjeta("Devueltas", conteo.get(EstadoUnidad.DEVUELTA), "tarjeta-gris"),
                tarjeta("Eventos pendientes SNT-PM", contexto.getIntegracion().cantidadPendientes(), "tarjeta-roja"));
        List<UnidadProducto> proximas = contexto.getServicioTrazabilidad().proximasAVencer(LocalDate.now());
        alertas.getChildren().clear();
        if (proximas.isEmpty()) {
            alertas.getChildren().add(new Label("No hay unidades dentro del umbral de alerta."));
        }
        for (UnidadProducto u : proximas) {
            long dias = contexto.getServicioTrazabilidad().diasParaVencer(u, LocalDate.now());
            alertas.getChildren().add(new Label("⚠ " + u.getProducto().getDescripcion() + " · serie "
                    + u.getNumeroSerie() + " vence el " + formatear(u.getVencimiento()) + " (" + dias + " días) · "
                    + (u.getUbicacionActual() == null ? "-" : u.getUbicacionActual().getNombre())));
        }
    }

    @Override
    public String getTituloPestana() {
        return "Inicio";
    }
}
