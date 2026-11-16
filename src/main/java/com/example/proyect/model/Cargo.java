package com.example.proyect.model;

import java.time.LocalDate;

public abstract class Cargo {

    private final String descripcion;
    private final LocalDate fecha;

    public Cargo(String descripcion, LocalDate fecha) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción del cargo es obligatoria");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del cargo es obligatoria");
        }
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public abstract long calcularValor();
}
