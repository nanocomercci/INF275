package com.trazapm.modelo;

/** Familia "Stent coronario": agrega diámetro y si es liberador de fármaco. */
public class StentCoronario extends Producto {

    public static final String TIPO = "Stent coronario";
    private final double diametroMm;
    private final boolean liberadorFarmaco;

    public StentCoronario(Long id, String gtin, String marca, String modelo, String certificadoAnmat,
                          double diametroMm, boolean liberadorFarmaco) {
        super(id, gtin, marca, modelo, certificadoAnmat);
        this.diametroMm = diametroMm;
        this.liberadorFarmaco = liberadorFarmaco;
    }

    public double getDiametroMm() {
        return diametroMm;
    }

    public boolean isLiberadorFarmaco() {
        return liberadorFarmaco;
    }

    @Override
    public String getTipoProducto() {
        return TIPO;
    }

    @Override
    public int getDiasAlertaVencimiento() {
        return liberadorFarmaco ? 120 : 90; // el fármaco acorta la vida útil efectiva
    }

    @Override
    public String getDetalleTecnico() {
        return String.format("%.2f mm%s", diametroMm, liberadorFarmaco ? " · liberador de fármaco" : "");
    }
}
