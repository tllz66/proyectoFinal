package com.example.proyect.model;

import java.time.LocalDate;

public class CargoPenalidad extends Cargo {

    private final String motivo;
    private final long monto;

    public CargoPenalidad(String descripcion, LocalDate fecha, String motivo, long monto) {
        super(descripcion, fecha);
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la penalidad es obligatorio");
        }
        this.motivo = motivo;
        this.monto = monto;
    }

    public String getMotivo() {
        return motivo;
    }

    public long getMonto() {
        return monto;
    }

    @Override
    public long calcularValor() {
        return monto;
    }
}
