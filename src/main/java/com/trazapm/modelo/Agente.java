package com.trazapm.modelo;

/** Agente externo de la cadena (proveedor, distribuidor) identificado por CUIT y GLN. */
public class Agente extends EntidadBase {

    private String razonSocial;
    private String cuit;
    private String glnCufe;
    private String tipoAgente;

    public Agente(Long id, String razonSocial, String cuit, String glnCufe, String tipoAgente) {
        super(id);
        this.razonSocial = razonSocial;
        this.cuit = cuit;
        this.glnCufe = glnCufe;
        this.tipoAgente = tipoAgente;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getCuit() {
        return cuit;
    }

    public String getGlnCufe() {
        return glnCufe;
    }

    public String getTipoAgente() {
        return tipoAgente;
    }

    @Override
    public String toString() {
        return razonSocial;
    }
}
