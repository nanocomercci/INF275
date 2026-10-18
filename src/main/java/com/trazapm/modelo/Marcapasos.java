package com.trazapm.modelo;

/** Familia "Marcapasos": agrega la cantidad de cámaras que estimula. */
public class Marcapasos extends Producto {

    public static final String TIPO = "Marcapasos";
    private final int camaras;

    public Marcapasos(Long id, String gtin, String marca, String modelo, String certificadoAnmat, int camaras) {
        super(id, gtin, marca, modelo, certificadoAnmat);
        this.camaras = camaras;
    }

    public int getCamaras() {
        return camaras;
    }

    @Override
    public String getTipoProducto() {
        return TIPO;
    }

    @Override
    public int getDiasAlertaVencimiento() {
        return 180; // reposición importada y de alto costo: aviso con seis meses
    }

    @Override
    public String getDetalleTecnico() {
        return camaras == 1 ? "Unicameral" : "Bicameral";
    }
}
