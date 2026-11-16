package com.example.proyect.model;

import java.time.LocalDateTime;

public record Novedad(LocalDateTime fecha, TipoNovedad tipo, String descripcion) {

    public Novedad {
        if (fecha == null || tipo == null) {
            throw new IllegalArgumentException("Fecha y tipo son obligatorios");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
    }
}
