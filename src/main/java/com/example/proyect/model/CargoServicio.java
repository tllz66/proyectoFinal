package com.example.proyect.model;

import java.time.LocalDate;

public class CargoServicio extends Cargo {

    private final String tipoServicio;
    private final int cantidad;
    private final long valorUnitario;

    public CargoServicio(String descripcion, LocalDate fecha, String tipoServicio,
                         int cantidad, long valorUnitario) {
        super(descripcion, fecha);
        if (tipoServicio == null || tipoServicio.isBlank()) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.tipoServicio = tipoServicio;
        this.cantidad = cantidad;
        this.valorUnitario = valorUnitario;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public long getValorUnitario() {
        return valorUnitario;
    }

    @Override
    public long calcularValor() {
        return cantidad * valorUnitario;
    }
}
