package com.trazapm.control;

import com.trazapm.modelo.EstadoUnidad;
import com.trazapm.modelo.Marcapasos;
import com.trazapm.modelo.ProtesisCadera;
import com.trazapm.modelo.StentCoronario;
import com.trazapm.modelo.UnidadProducto;
import java.util.List;

/**
 * Tablero de stock por familia y estado (CU-08).
 *
 * <p>Uso complementario de arreglos y ArrayList: las unidades llegan desde la
 * base en un {@code ArrayList} porque su cantidad es variable, mientras que
 * el resumen se acumula en una matriz {@code int[][]} porque sus dimensiones
 * son fijas y conocidas de antemano (3 familias del alcance × 6 estados del
 * enumerado). Así cada conteo es un acceso directo por índice, sin búsquedas.
 */
public class ReporteStock {

    /** Familias del alcance, en el orden de las filas de la matriz. */
    public static final String[] FAMILIAS = {Marcapasos.TIPO, StentCoronario.TIPO, ProtesisCadera.TIPO};

    /** Estados de la unidad, en el orden de las columnas de la matriz. */
    public static final EstadoUnidad[] ESTADOS = EstadoUnidad.values();

    private final int[][] conteo = new int[FAMILIAS.length][ESTADOS.length];

    /** Recorre la lista una sola vez y acumula cada unidad en su celda. */
    public ReporteStock(List<UnidadProducto> unidades) {
        for (UnidadProducto u : unidades) {
            int fila = indiceFamilia(u.getProducto().getTipoProducto());
            if (fila >= 0) {
                conteo[fila][u.getEstado().ordinal()]++;
            }
        }
    }

    /** Búsqueda secuencial en el arreglo de familias; devuelve -1 si no pertenece al alcance. */
    static int indiceFamilia(String tipo) {
        for (int i = 0; i < FAMILIAS.length; i++) {
            if (FAMILIAS[i].equals(tipo)) {
                return i;
            }
        }
        return -1;
    }

    public int cantidad(int familia, int estado) {
        return conteo[familia][estado];
    }

    /** Suma de una fila: unidades de la familia en cualquier estado. */
    public int totalFamilia(int familia) {
        int total = 0;
        for (int valor : conteo[familia]) {
            total += valor;
        }
        return total;
    }

    /** Suma de una columna: unidades de todas las familias en ese estado. */
    public int totalEstado(int estado) {
        int total = 0;
        for (int[] fila : conteo) {
            total += fila[estado];
        }
        return total;
    }

    public int total() {
        int total = 0;
        for (int i = 0; i < FAMILIAS.length; i++) {
            total += totalFamilia(i);
        }
        return total;
    }

    /** Unidades que siguen en el establecimiento (estados no finales). */
    public int enStock() {
        int total = 0;
        for (int j = 0; j < ESTADOS.length; j++) {
            if (!ESTADOS[j].esFinal()) {
                total += totalEstado(j);
            }
        }
        return total;
    }

    /** Copia defensiva: quien la reciba no puede alterar el reporte. */
    public int[][] getMatriz() {
        int[][] copia = new int[conteo.length][];
        for (int i = 0; i < conteo.length; i++) {
            copia[i] = conteo[i].clone();
        }
        return copia;
    }
}
