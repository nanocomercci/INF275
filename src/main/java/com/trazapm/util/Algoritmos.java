package com.trazapm.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Algoritmos de ordenación y búsqueda implementados en forma explícita
 * (en lugar de Collections.sort) para mostrar su funcionamiento.
 * Son genéricos: reciben el criterio como Comparator, por lo que sirven
 * para ordenar unidades por vencimiento, por serie o por cualquier campo.
 */
public final class Algoritmos {

    private Algoritmos() {
    }

    /**
     * Ordenación por inserción: estable y eficiente para listas chicas o casi
     * ordenadas, que es el caso del stock de implantables de un establecimiento.
     * No modifica la lista original: devuelve una copia ordenada.
     */
    public static <T> List<T> ordenarPorInsercion(List<T> origen, Comparator<? super T> criterio) {
        List<T> lista = new ArrayList<>(origen);
        for (int i = 1; i < lista.size(); i++) {
            T actual = lista.get(i);
            int j = i - 1;
            // Desplaza a la derecha los elementos mayores que "actual".
            while (j >= 0 && criterio.compare(lista.get(j), actual) > 0) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
        return lista;
    }

    /**
     * Búsqueda binaria sobre una lista YA ordenada por la misma clave.
     * Divide el intervalo a la mitad en cada paso (O(log n)).
     *
     * @return índice del elemento encontrado o -1 si no existe
     */
    public static <T, K extends Comparable<K>> int busquedaBinaria(List<T> ordenada, K clave, Function<T, K> extractor) {
        int inicio = 0;
        int fin = ordenada.size() - 1;
        while (inicio <= fin) {
            int medio = (inicio + fin) >>> 1;
            int comparacion = extractor.apply(ordenada.get(medio)).compareTo(clave);
            if (comparacion == 0) {
                return medio;
            } else if (comparacion < 0) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return -1;
    }
}
