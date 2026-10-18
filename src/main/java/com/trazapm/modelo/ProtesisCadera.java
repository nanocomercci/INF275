package com.trazapm.modelo;

/** Familia "Prótesis de cadera": agrega el material del componente. */
public class ProtesisCadera extends Producto {

    public static final String TIPO = "Prótesis de cadera";
    private final String material;

    public ProtesisCadera(Long id, String gtin, String marca, String modelo, String certificadoAnmat, String material) {
        super(id, gtin, marca, modelo, certificadoAnmat);
        this.material = material;
    }

    public String getMaterial() {
        return material;
    }

    @Override
    public String getTipoProducto() {
        return TIPO;
    }

    @Override
    public int getDiasAlertaVencimiento() {
        return 60; // reposición nacional más rápida
    }

    @Override
    public String getDetalleTecnico() {
        return "Material: " + material;
    }
}
