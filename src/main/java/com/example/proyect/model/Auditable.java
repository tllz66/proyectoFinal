package com.example.proyect.model;

import java.util.List;

public interface Auditable {

    void registrarNovedad(Novedad novedad);

    List<Novedad> obtenerNovedades();
}
