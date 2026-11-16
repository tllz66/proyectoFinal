package com.example.proyect.model;

import java.time.LocalDate;

public record Bloqueo(String codigoApartamento, LocalDate fechaInicio,
                      LocalDate fechaFin, String motivo) {

    public Bloqueo {
        if (codigoApartamento == null || codigoApartamento.isBlank()) {
            throw new IllegalArgumentException("El código del apartamento es obligatorio");
        }
        if (fechaInicio == null || fechaFin == null || !fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior al inicio");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del bloqueo es obligatorio");
        }
    }

    public boolean seCruzaCon(LocalDate entrada, LocalDate salida) {
        return fechaInicio.isBefore(salida) && entrada.isBefore(fechaFin);
    }
}
