package com.trazapm.modelo;

/**
 * Producto médico implantable identificado por GTIN (nivel "modelo").
 * Es abstracta: el sistema solo trabaja con las tres familias del alcance
 * (Marcapasos, StentCoronario, ProtesisCadera). Cada subclase define sus
 * características propias redefiniendo los métodos abstractos.
 */
public abstract class Producto extends EntidadBase {

    private String gtin;
    private String marca;
    private String modelo;
    private String certificadoAnmat;

    protected Producto(Long id, String gtin, String marca, String modelo, String certificadoAnmat) {
        super(id);
        this.gtin = gtin;
        this.marca = marca;
        this.modelo = modelo;
        this.certificadoAnmat = certificadoAnmat;
    }

    /** Nombre de la familia, igual al valor de la tabla tipo_producto. */
    public abstract String getTipoProducto();

    /**
     * Días de anticipación con que debe alertarse el vencimiento. Varía por
     * familia porque el tiempo de reposición y el riesgo no son iguales.
     */
    public abstract int getDiasAlertaVencimiento();

    /** Dato técnico distintivo de la familia, para mostrar en pantalla. */
    public abstract String getDetalleTecnico();

    public String getGtin() {
        return gtin;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getCertificadoAnmat() {
        return certificadoAnmat;
    }

    public String getDescripcion() {
        return getTipoProducto() + " " + marca + " " + modelo;
    }

    @Override
    public String toString() {
        return gtin + " - " + getDescripcion();
    }
}
