package com.trazapm;

import com.trazapm.config.Contexto;
import com.trazapm.vista.VentanaPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Punto de entrada JavaFX. Arma el contexto (conexión MySQL o modo memoria)
 * y muestra la ventana principal. Con el argumento --memoria se fuerza el
 * modo demostración aunque exista una base configurada.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        boolean forzarMemoria = getParameters().getRaw().contains("--memoria");
        Contexto contexto = new Contexto(forzarMemoria);
        VentanaPrincipal ventana = new VentanaPrincipal(stage, contexto);
        Scene escena = new Scene(ventana.getRaiz(), 1280, 800);
        escena.getStylesheets().add(App.class.getResource("/trazapm.css").toExternalForm());
        stage.setTitle("TrazaPM — Inicio");
        stage.setScene(escena);
        stage.show();
        if (contexto.getAvisoConexion() != null) {
            Alert aviso = new Alert(Alert.AlertType.WARNING, contexto.getAvisoConexion());
            aviso.setHeaderText("Base de datos no disponible");
            aviso.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
