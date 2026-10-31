package com.trazapm.vista;

import com.trazapm.config.Contexto;
import com.trazapm.excepciones.DatosInvalidosException;
import com.trazapm.excepciones.PersistenciaException;
import com.trazapm.excepciones.TrazabilidadException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Superclase abstracta de todas las pantallas. Resuelve lo común (encabezado,
 * secciones con título, formato de fechas y presentación de errores) y obliga
 * a cada panel a implementar {@link #refrescar()}, que la ventana principal
 * invoca de forma polimórfica cada vez que se selecciona una pestaña.
 */
public abstract class PantallaBase extends ScrollPane {

    protected static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    protected final Contexto contexto;
    protected final VBox contenido = new VBox(14);

    protected PantallaBase(Contexto contexto, String titulo, String subtitulo) {
        this.contexto = contexto;
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("titulo-panel");
        Label lblSubtitulo = new Label(subtitulo);
        lblSubtitulo.getStyleClass().add("subtitulo-panel");
        contenido.getChildren().add(new VBox(2, lblTitulo, lblSubtitulo));
        contenido.setPadding(new Insets(18, 22, 18, 22));
        setContent(contenido);
        setFitToWidth(true);
    }

    /** Recarga los datos que muestra el panel. */
    public abstract void refrescar();

    /** Título corto que usa la pestaña. */
    public abstract String getTituloPestana();

    /** Crea un recuadro con encabezado gris, como en los prototipos. */
    protected VBox seccion(String titulo, Node cuerpo) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("seccion-titulo");
        encabezado.setMaxWidth(Double.MAX_VALUE);
        VBox caja = new VBox(encabezado, cuerpo);
        cuerpo.getStyleClass().add("seccion-cuerpo");
        caja.getStyleClass().add("seccion");
        VBox.setVgrow(cuerpo, Priority.ALWAYS);
        return caja;
    }

    /** Agrega a la grilla una etiqueta arriba y el control debajo. */
    protected static void campo(GridPane grilla, String etiqueta, Node control, int columna, int fila) {
        Label lbl = new Label(etiqueta);
        lbl.getStyleClass().add("campo-etiqueta");
        grilla.add(new VBox(4, lbl, control), columna, fila);
    }

    protected static GridPane grilla(int columnas) {
        GridPane g = new GridPane();
        g.setHgap(28);
        g.setVgap(10);
        for (int i = 0; i < columnas; i++) {
            javafx.scene.layout.ColumnConstraints c = new javafx.scene.layout.ColumnConstraints();
            c.setPercentWidth(100.0 / columnas);
            c.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(c);
        }
        return g;
    }

    /**
     * Convierte el texto de fecha ingresado (dd/MM/aaaa). Si el formato no es
     * válido captura DateTimeParseException y la traduce a un error de negocio
     * con un mensaje comprensible para el operador.
     */
    protected static LocalDate leerFecha(String texto, String nombreCampo) throws DatosInvalidosException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException(java.util.List.of(
                    "La fecha de " + nombreCampo + " \"" + texto.trim() + "\" no es válida. Use el formato dd/mm/aaaa."));
        }
    }

    protected static String formatear(LocalDate f) {
        return f == null ? "-" : FECHA.format(f);
    }

    protected static String formatear(LocalDateTime f) {
        return f == null ? "-" : FECHA_HORA.format(f);
    }

    /** Muestra el mensaje en el recuadro indicado aplicando el estilo según el tipo de resultado. */
    protected static void mostrar(Label destino, String texto, String estilo) {
        destino.getStyleClass().removeAll("mensaje-info", "mensaje-ok", "mensaje-error");
        destino.getStyleClass().add(estilo);
        destino.setText(texto);
    }

    /**
     * Abre el diálogo "Guardar como" del sistema operativo con un nombre
     * sugerido. Devuelve vacío si el usuario cancela.
     */
    protected Optional<Path> elegirArchivoDestino(String nombreSugerido, String extension) {
        FileChooser dialogo = new FileChooser();
        dialogo.setTitle("Exportar reporte");
        dialogo.setInitialFileName(nombreSugerido + "." + extension);
        dialogo.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                extension.toUpperCase() + " (*." + extension + ")", "*." + extension));
        File elegido = dialogo.showSaveDialog(getScene() == null ? null : getScene().getWindow());
        return elegido == null ? Optional.empty() : Optional.of(elegido.toPath());
    }

    /** Traducción única de excepciones a mensajes de pantalla. */
    protected static void mostrarError(Label destino, Exception e) {
        if (e instanceof TrazabilidadException) {
            mostrar(destino, e.getMessage(), "mensaje-error");
        } else if (e instanceof PersistenciaException) {
            mostrar(destino, "Error de base de datos: " + e.getMessage()
                    + (e.getCause() != null ? " (" + e.getCause().getMessage() + ")" : ""), "mensaje-error");
        } else {
            mostrar(destino, "Error inesperado: " + e, "mensaje-error");
        }
    }
}
