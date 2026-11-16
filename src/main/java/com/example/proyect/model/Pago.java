package com.example.proyect.model;

import java.time.LocalDate;

public record Pago(long monto, MedioPago medio, LocalDate fecha) {

    public Pago {
        if (monto == 0) {
            throw new IllegalArgumentException("El monto del pago no puede ser cero");
        }
        if (medio == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del pago es obligatoria");
        }
    }

    public boolean esDevolucion() {
        return monto < 0;
    }
}
