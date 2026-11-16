package com.example.proyect.model;

import java.time.LocalDate;

public class CargoAlojamiento extends Cargo {

    private final int noches;
    private final long valorEstancia;

    public CargoAlojamiento(String descripcion, LocalDate fecha, int noches, long valorEstancia) {
        super(descripcion, fecha);
        if (noches <= 0) {
            throw new IllegalArgumentException("Las noches deben ser mayores que cero");
        }
        this.noches = noches;
        this.valorEstancia = valorEstancia;
    }

    public int getNoches() {
        return noches;
    }

    public long getValorEstancia() {
        return valorEstancia;
    }

    @Override
    public long calcularValor() {
        return valorEstancia;
    }
}
