package com.trazapm.vista;

import com.trazapm.config.Contexto;
import com.trazapm.modelo.Usuario;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Ventana principal: barra de menú, barra de herramientas, pestañas y barra
 * de estado. Es el "menú de selección" del prototipo: cada opción del menú,
 * cada botón y cada pestaña lleva a una pantalla. Todas las pantallas se
 * guardan en una lista de PantallaBase y se refrescan de forma polimórfica.
 */
public class VentanaPrincipal {

    private final Contexto contexto;
    private final Stage stage;
    private final TabPane pestanas = new TabPane();
    private final List<PantallaBase> paneles = new ArrayList<>();
    private final Label lblEstado = new Label();
    private final Label lblConector = new Label();
    private final BorderPane raiz = new BorderPane();

    public VentanaPrincipal(Stage stage, Contexto contexto) {
        this.stage = stage;
        this.contexto = contexto;
        PantallaTrazabilidad trazabilidad = new PantallaTrazabilidad(contexto);
        paneles.add(new PantallaInicio(contexto, this::irA));
        paneles.add(new PantallaRecepcion(contexto, this::actualizarEstado));
        paneles.add(new PantallaMovimientos(contexto, this::actualizarEstado));
        paneles.add(trazabilidad);
        paneles.add(new PantallaStock(contexto));
        paneles.add(new PantallaIntegracion(contexto, this::actualizarEstado));

        for (PantallaBase p : paneles) {
            Tab t = new Tab(p.getTituloPestana(), p);
            t.setClosable(false);
            pestanas.getTabs().add(t);
        }
        // Al cambiar de pestaña se refresca el panel elegido (enlace dinámico a su refrescar()).
        pestanas.getSelectionModel().selectedIndexProperty().addListener((obs, ant, nuevo) -> {
            PantallaBase p = paneles.get(nuevo.intValue());
            p.refrescar();
            stage.setTitle("TrazaPM — " + p.getTituloPestana());
        });

        raiz.setTop(new VBox(crearMenu(), crearBarraHerramientas()));
        raiz.setCenter(pestanas);
        raiz.setBottom(crearBarraEstado());
        paneles.get(0).refrescar();
        actualizarEstado();
    }

    public BorderPane getRaiz() {
        return raiz;
    }

    public TabPane getPestanas() {
        return pestanas;
    }

    public void irA(int indice) {
        pestanas.getSelectionModel().select(indice);
    }

    private MenuBar crearMenu() {
        Menu archivo = new Menu("Archivo");
        MenuItem salir = new MenuItem("Salir");
        salir.setOnAction(e -> Platform.exit());
        archivo.getItems().add(salir);

        Menu operaciones = new Menu("Operaciones");
        operaciones.getItems().addAll(item("Registrar recepción", 1), item("Registrar movimiento", 2));
        Menu consultas = new Menu("Consultas");
        consultas.getItems().addAll(item("Inicio e indicadores", 0), item("Consultar trazabilidad", 3),
                item("Stock y vencimientos", 4));
        Menu administracion = new Menu("Administración");
        MenuItem caida = new MenuItem("Simular caída / restablecer SNT-PM");
        caida.setOnAction(e -> {
            contexto.getConector().setDisponible(!contexto.getConector().isDisponible());
            actualizarEstado();
        });
        administracion.getItems().addAll(item("Cola de integración", 5), new SeparatorMenuItem(), caida);
        Menu ayuda = new Menu("Ayuda");
        MenuItem acerca = new MenuItem("Acerca de TrazaPM");
        acerca.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText("TrazaPM - prototipo TP3");
            a.setContentText("Seminario de Práctica Informática (INF275)\nLuciano Felipe Comercci\n"
                    + "Datos sintéticos. Integración con SNT-PM simulada.");
            a.showAndWait();
        });
        ayuda.getItems().add(acerca);
        return new MenuBar(archivo, operaciones, consultas, administracion, ayuda);
    }

    private MenuItem item(String texto, int pestana) {
        MenuItem m = new MenuItem(texto);
        m.setOnAction(e -> irA(pestana));
        return m;
    }

    private HBox crearBarraHerramientas() {
        Button nueva = boton("+  Nueva recepción", 1);
        Button movimiento = boton("⇄  Movimiento", 2);
        Button consultar = boton("?  Consultar", 3);
        Button stock = boton("▤  Stock", 4);
        Button sincronizar = boton("↻  Sincronizar", 5);
        ComboBox<Usuario> cbUsuario = new ComboBox<>();
        cbUsuario.setId("cbUsuario");
        cbUsuario.getItems().setAll(contexto.getDatos().catalogo().listarUsuarios());
        cbUsuario.setValue(contexto.getUsuarioActual());
        cbUsuario.setOnAction(e -> {
            contexto.setUsuarioActual(cbUsuario.getValue());
            paneles.get(pestanas.getSelectionModel().getSelectedIndex()).refrescar();
            actualizarEstado();
        });
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(8, nueva, movimiento, consultar, stock, sincronizar, espacio,
                new Label("Usuario:"), cbUsuario);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-herramientas");
        return barra;
    }

    private Button boton(String texto, int pestana) {
        Button b = new Button(texto);
        b.setOnAction(e -> irA(pestana));
        return b;
    }

    private HBox crearBarraEstado() {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        lblConector.setId("lblConector");
        HBox barra = new HBox(10, lblEstado, espacio, lblConector,
                new Label("· Java " + System.getProperty("java.specification.version")
                        + " · JavaFX " + System.getProperty("javafx.runtime.version", "").split("[-+]")[0]));
        barra.getStyleClass().add("barra-estado");
        return barra;
    }

    /** Actualiza la barra de estado tras cada operación. */
    public void actualizarEstado() {
        lblEstado.setText(contexto.getDatos().getDescripcion() + " · " + contexto.getUsuarioActual().getNombreUsuario());
        lblConector.setText("SNT-PM simulado: " + (contexto.getConector().isDisponible() ? "disponible" : "SIN SERVICIO")
                + " · Pendientes: " + contexto.getIntegracion().cantidadPendientes());
    }
}
