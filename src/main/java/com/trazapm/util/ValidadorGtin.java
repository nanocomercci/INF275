package com.trazapm.util;

/**
 * Valida el código GTIN-14 según el algoritmo de dígito verificador de GS1
 * (módulo 10): desde la derecha, los dígitos en posición impar se
 * multiplican por 3 y los pares por 1; el verificador completa la decena.
 */
public final class ValidadorGtin {

    private ValidadorGtin() {
    }

    public static boolean esValido(String gtin) {
        if (gtin == null || gtin.length() != 14) {
            return false;
        }
        int suma = 0;
        // Se recorren los 13 primeros dígitos de derecha a izquierda.
        for (int i = 12; i >= 0; i--) {
            char c = gtin.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int digito = c - '0';
            boolean posicionImpar = (12 - i) % 2 == 0;
            suma += posicionImpar ? digito * 3 : digito;
        }
        char ultimo = gtin.charAt(13);
        if (!Character.isDigit(ultimo)) {
            return false;
        }
        int verificador = (10 - suma % 10) % 10;
        return verificador == ultimo - '0';
    }
}
