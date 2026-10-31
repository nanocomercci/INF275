package com.trazapm.vista;

import com.trazapm.config.Contexto;
import com.trazapm.control.ReporteStock;
import com.trazapm.control.ServicioTrazabilidad;
import com.trazapm.control.exportacion.Exportador;
import com.trazapm.excepciones.TrazabilidadException;
import java.nio.file.Path;
import javafx.scene.layout.GridPane;
import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.UnidadProducto;
import com.trazapm.util.Algoritmos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Pantalla del CU-08 "Generar reportes y alertas" (stock y vencimientos). Muestra el uso de los
 * algoritmos de ordenación por inserción y búsqueda binaria, y el manejo de
 * NumberFormatException al ingresar la cantidad de días.
 */
public class PantallaStock extends PantallaBase {

    private static final String TODOS = "Todos los estados";

    private final ComboBox<String> cbEstado = new ComboBox<>();
    private final TextField txtDias = new TextField("365");
    private final TextField txtSerie = new TextField();
    private final TableView<UnidadProducto> tabla = new TableView<>();
    private final Label lblMensaje = new Label();
    private final GridPane tablero = new GridPane();
    private List<UnidadProducto> vista = new ArrayList<>();

    public PantallaStock(Contexto contexto) {
        super(contexto, "Stock y vencimientos", "CU-08 · Unidades del establecimiento, alertas de vencimiento y búsqueda rápida.");
        cbEstado.setId("cbEstadoStock");
        txtDias.setId("txtDiasStock");
        txtSerie.setId("txtSerieStock");
        lblMensaje.setId("lblMensajeStock");
        cbEstado.getItems().add(TODOS);
        for (EstadoUnidad e : EstadoUnidad.values()) {
            cbEstado.getItems().add(e.getEtiqueta());
        }
        cbEstado.getSelectionModel().select(0);
        cbEstado.setOnAction(e -> refrescar());
        txtDias.setPrefColumnCount(5);
        txtSerie.setPromptText("Número de serie");

        Button btnVencen = new Button("Vencen en los próximos N días");
        btnVencen.setId("btnVencenStock");
        btnVencen.getStyleClass().add("boton-secundario");
        btnVencen.setOnAction(e -> filtrarPorDias());
        Button btnAlertas = new Button("Alertas por familia");
        btnAlertas.setId("btnAlertasStock");
        btnAlertas.getStyleClass().add("boton-secundario");
        btnAlertas.setOnAction(e -> mostrarAlertas());
        Button btnOrdenar = new Button("Ordenar por vencimiento");
        btnOrdenar.setId("btnOrdenarStock");
        btnOrdenar.getStyleClass().add("boton-secundario");
        btnOrdenar.setOnAction(e -> ordenarPorVencimiento());
        Button btnBuscar = new Button("Buscar serie");
        btnBuscar.setId("btnBuscarSerieStock");
        btnBuscar.getStyleClass().add("boton-primario");
        btnBuscar.setOnAction(e -> buscarSerie());

        HBox filtros1 = new HBox(10, new Label("Estado:"), cbEstado, new Label("Días:"), txtDias, btnVencen, btnAlertas);
        filtros1.setAlignment(Pos.CENTER_LEFT);
        Button btnExportar = new Button("Exportar CSV");
        btnExportar.setId("btnExportarStock");
        btnExportar.getStyleClass().add("boton-secundario");
        btnExportar.setOnAction(e -> exportar());
        HBox filtros2 = new HBox(10, btnOrdenar, new Label("   Búsqueda binaria por serie:"), txtSerie, btnBuscar, btnExportar);
        filtros2.setAlignment(Pos.CENTER_LEFT);

        LocalDate hoy = LocalDate.now();
        ServicioTrazabilidad st = contexto.getServicioTrazabilidad();
        tabla.getColumns().add(PantallaTrazabilidad.columna("GTIN", (UnidadProducto u) -> u.getProducto().getGtin(), 128));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Familia", (UnidadProducto u) -> u.getProducto().getTipoProducto(), 132));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Modelo", (UnidadProducto u) -> u.getProducto().getMarca() + " " + u.getProducto().getModelo(), 165));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Detalle técnico", (UnidadProducto u) -> u.getProducto().getDetalleTecnico(), 215));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Serie", UnidadProducto::getNumeroSerie, 118));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Vence", (UnidadProducto u) -> formatear(u.getVencimiento()), 90));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Días", (UnidadProducto u) -> String.valueOf(st.diasParaVencer(u, hoy)), 50));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Estado", (UnidadProducto u) -> u.getEstado().getEtiqueta(), 100));
        tabla.getColumns().add(PantallaTrazabilidad.columna("Ubicación", (UnidadProducto u) ->
                u.getUbicacionActual() == null ? "-" : u.getUbicacionActual().getNombre(), 170));
        tabla.setPrefHeight(330);
        // Resalta en amarillo las unidades dentro del umbral de alerta de su familia.
        tabla.setRowFactory(tv -> new TableRow<UnidadProducto>() {
            @Override
            protected void updateItem(UnidadProducto u, boolean vacia) {
                super.updateItem(u, vacia);
                getStyleClass().removeAll("fila-alerta", "fila-vencida");
                if (!vacia && u != null && !u.getEstado().esFinal()) {
                    long dias = st.diasParaVencer(u, LocalDate.now());
                    if (dias < 0) {
                        getStyleClass().add("fila-vencida");
                    } else if (dias <= u.getProducto().getDiasAlertaVencimiento()) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            }
        });

        lblMensaje.setMaxWidth(Double.MAX_VALUE);
        lblMensaje.setWrapText(true);
        tablero.setId("tableroStock");
        tablero.getStyleClass().add("tablero");
        tablero.setHgap(18);
        tablero.setVgap(6);
        contenido.getChildren().addAll(seccion("Tablero por familia y estado", tablero),
                seccion("Filtros y herramientas", new VBox(10, filtros1, filtros2)),
                seccion("Unidades", tabla), lblMensaje);
    }

    private void cargar(List<UnidadProducto> unidades, String mensaje) {
        vista = unidades;
        tabla.getItems().setAll(unidades);
        mostrar(lblMensaje, mensaje, "mensaje-info");
    }

    private List<UnidadProducto> filtrarPorEstado() {
        String elegido = cbEstado.getValue();
        List<UnidadProducto> resultado = new ArrayList<>();
        for (UnidadProducto u : contexto.getServicioTrazabilidad().listarUnidades()) {
            if (TODOS.equals(elegido) || u.getEstado().getEtiqueta().equals(elegido)) {
                resultado.add(u);
            }
        }
        return resultado;
    }

    /**
     * Lee la cantidad de días. Integer.parseInt lanza NumberFormatException si
     * el texto no es numérico: se captura y se informa sin interrumpir la pantalla.
     */
    private void filtrarPorDias() {
        int dias;
        try {
            dias = Integer.parseInt(txtDias.getText().trim());
            if (dias < 0) {
                throw new NumberFormatException("negativo");
            }
        } catch (NumberFormatException e) {
            mostrar(lblMensaje, "\"" + txtDias.getText() + "\" no es una cantidad de días válida. Ingrese un número entero positivo.", "mensaje-error");
            return;
        }
        LocalDate hoy = LocalDate.now();
        List<UnidadProducto> resultado = new ArrayList<>();
        for (UnidadProducto u : filtrarPorEstado()) {
            if (!u.getEstado().esFinal() && contexto.getServicioTrazabilidad().diasParaVencer(u, hoy) <= dias) {
                resultado.add(u);
            }
        }
        resultado = Algoritmos.ordenarPorInsercion(resultado, Comparator.comparing(UnidadProducto::getVencimiento));
        cargar(resultado, resultado.size() + " unidades en stock vencen dentro de " + dias + " días (ordenadas por vencimiento).");
    }

    private void mostrarAlertas() {
        List<UnidadProducto> alertas = contexto.getServicioTrazabilidad().proximasAVencer(LocalDate.now());
        cargar(alertas, alertas.size() + " unidades en alerta según el umbral de su familia "
                + "(marcapasos 180 días, stent 90/120, prótesis 60).");
    }

    private void ordenarPorVencimiento() {
        cargar(Algoritmos.ordenarPorInsercion(vista, Comparator.comparing(UnidadProducto::getVencimiento,
                Comparator.nullsLast(Comparator.naturalOrder()))), "Listado ordenado por vencimiento (ordenación por inserción).");
    }

    private void buscarSerie() {
        if (txtSerie.getText().isBlank()) {
            mostrar(lblMensaje, "Ingrese un número de serie.", "mensaje-error");
            return;
        }
        Optional<UnidadProducto> u = contexto.getServicioTrazabilidad().buscarPorSerie(txtSerie.getText());
        if (u.isPresent()) {
            if (!tabla.getItems().contains(u.get())) {
                cargar(filtrarPorEstado(), "");
            }
            tabla.getSelectionModel().select(u.get());
            tabla.scrollTo(u.get());
            mostrar(lblMensaje, "Serie encontrada mediante búsqueda binaria: " + u.get(), "mensaje-ok");
        } else {
            mostrar(lblMensaje, "No existe una unidad con serie " + txtSerie.getText().trim().toUpperCase() + ".", "mensaje-error");
        }
    }

    /**
     * Dibuja la matriz int[][] del reporte: una fila por familia, una columna
     * por estado, más los totales calculados recorriendo filas y columnas.
     */
    private void dibujarTablero(ReporteStock r) {
        tablero.getChildren().clear();
        tablero.add(encabezado("Familia"), 0, 0);
        for (int j = 0; j < ReporteStock.ESTADOS.length; j++) {
            tablero.add(encabezado(ReporteStock.ESTADOS[j].getEtiqueta()), j + 1, 0);
        }
        int colTotal = ReporteStock.ESTADOS.length + 1;
        tablero.add(encabezado("Total"), colTotal, 0);
        for (int i = 0; i < ReporteStock.FAMILIAS.length; i++) {
            tablero.add(new Label(ReporteStock.FAMILIAS[i]), 0, i + 1);
            for (int j = 0; j < ReporteStock.ESTADOS.length; j++) {
                tablero.add(new Label(String.valueOf(r.cantidad(i, j))), j + 1, i + 1);
            }
            tablero.add(encabezado(String.valueOf(r.totalFamilia(i))), colTotal, i + 1);
        }
        int filaTotal = ReporteStock.FAMILIAS.length + 1;
        tablero.add(encabezado("Total"), 0, filaTotal);
        for (int j = 0; j < ReporteStock.ESTADOS.length; j++) {
            tablero.add(encabezado(String.valueOf(r.totalEstado(j))), j + 1, filaTotal);
        }
        tablero.add(encabezado(r.total() + " (" + r.enStock() + " en stock)"), colTotal, filaTotal);
    }

    private static Label encabezado(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    /** Exporta el listado visible y el tablero a CSV (CU-08, paso 3). */
    private void exportar() {
        Exportador exportador = contexto.getExportador();
        Optional<Path> destino = elegirArchivoDestino("stock_" + LocalDate.now(), exportador.getExtension());
        if (destino.isEmpty()) {
            return;
        }
        try {
            exportador.exportarStock(vista, contexto.getServicioTrazabilidad().generarReporteStock(), destino.get());
            mostrar(lblMensaje, vista.size() + " unidades exportadas en " + destino.get(), "mensaje-ok");
        } catch (TrazabilidadException e) {
            mostrarError(lblMensaje, e);
        }
    }

    @Override
    public void refrescar() {
        dibujarTablero(contexto.getServicioTrazabilidad().generarReporteStock());
        List<UnidadProducto> unidades = filtrarPorEstado();
        cargar(unidades, unidades.size() + " unidades. Las filas amarillas están dentro del umbral de alerta de vencimiento.");
    }

    @Override
    public String getTituloPestana() {
        return "Stock y vencimientos";
    }
}
