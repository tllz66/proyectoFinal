package com.example.proyect.model;

import java.time.LocalDate;
import java.time.Period;

public record Ocupante(String nombre, LocalDate fechaNacimiento) {

    public Ocupante {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del ocupante es obligatorio");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
    }

    public int calcularEdad(LocalDate fechaEntrada) {
        Period periodo = Period.between(fechaNacimiento, fechaEntrada);
        return periodo.getYears();
    }
}
