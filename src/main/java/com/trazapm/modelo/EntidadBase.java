package com.trazapm.modelo;

/**
 * Superclase abstracta de todas las entidades persistentes de TrazaPM.
 * Concentra el identificador técnico, que coincide con la clave primaria
 * AUTO_INCREMENT de cada tabla MySQL. Es abstracta porque no tiene sentido
 * instanciar una "entidad" genérica: siempre se crea una clase concreta.
 */
public abstract class EntidadBase {

    private Long id;

    protected EntidadBase() {
    }

    protected EntidadBase(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    /** El DAO asigna el id una vez que la entidad fue guardada. */
    public void setId(Long id) {
        this.id = id;
    }

    public boolean esNueva() {
        return id == null;
    }

    /** Dos entidades son iguales si son de la misma clase y tienen el mismo id. */
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass() || id == null) {
            return false;
        }
        return id.equals(((EntidadBase) otro).id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}
