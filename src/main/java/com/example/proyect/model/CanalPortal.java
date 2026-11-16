package com.example.proyect.model;


public class CanalPortal implements CanalReserva {

    private final double porcentajeAnticipo;

    public CanalPortal() {
        this(0.30);
    }

    public CanalPortal(double porcentajeAnticipo) {
        if (porcentajeAnticipo <= 0 || porcentajeAnticipo > 1) {
            throw new IllegalArgumentException("El porcentaje de anticipo debe estar entre 0 y 1");
        }
        this.porcentajeAnticipo = porcentajeAnticipo;
    }

    @Override
    public boolean exigeAnticipo() {
        return true;
    }

    @Override
    public long calcularAnticipo(long valorEstancia) {
        return Math.round(valorEstancia * porcentajeAnticipo);
    }

    @Override
    public long calcularComision(long valorEstancia) {
        return 0;
    }

    @Override
    public TipoCanal getTipo() {
        return TipoCanal.PORTAL;
    }
}
