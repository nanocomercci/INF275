package com.trazapm.excepciones;

import java.util.Collections;
import java.util.List;

/** Se lanza cuando uno o más campos no superan la validación; conserva la lista completa. */
public class DatosInvalidosException extends TrazabilidadException {

    private static final long serialVersionUID = 1L;

    private final transient List<String> errores;

    public DatosInvalidosException(List<String> errores) {
        super("Datos inválidos:\n- " + String.join("\n- ", errores));
        this.errores = Collections.unmodifiableList(errores);
    }

    public List<String> getErrores() {
        return errores;
    }
}
