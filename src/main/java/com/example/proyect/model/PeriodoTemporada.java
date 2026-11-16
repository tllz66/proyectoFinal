package com.example.proyect.model;

import java.time.LocalDate;

public record PeriodoTemporada(Temporada temporada, LocalDate fechaInicio, LocalDate fechaFin) {

    public PeriodoTemporada {
        if (temporada == null || fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Temporada y fechas son obligatorias");
        }
        if (temporada == Temporada.BAJA) {
            throw new IllegalArgumentException("La temporada Baja es la base y no lleva periodos");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior al inicio");
        }
    }

    public boolean contiene(LocalDate dia) {
        boolean noEsAntes = !dia.isBefore(fechaInicio);
        boolean noEsDespues = !dia.isAfter(fechaFin);
        return noEsAntes && noEsDespues;
    }
}
