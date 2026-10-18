package com.trazapm;

/**
 * Clase principal alternativa para ejecutar desde el IDE sin configurar
 * módulos: al no extender Application, la JVM no exige el module-path de JavaFX.
 */
public final class Lanzador {

    private Lanzador() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}
