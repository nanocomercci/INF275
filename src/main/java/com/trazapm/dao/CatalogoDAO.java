package com.trazapm.dao;

import com.trazapm.modelo.Agente;
import com.trazapm.modelo.Profesional;
import com.trazapm.modelo.Ubicacion;
import com.trazapm.modelo.Usuario;
import java.util.List;

/** Consultas de las tablas maestras que alimentan los combos de la interfaz. */
public interface CatalogoDAO {

    List<Ubicacion> listarUbicaciones();

    List<Agente> listarAgentes();

    List<Profesional> listarProfesionales();

    List<Usuario> listarUsuarios();
}
