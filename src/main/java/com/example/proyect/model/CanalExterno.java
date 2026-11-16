package com.example.proyect.model;


public class CanalExterno implements CanalReserva {

    private final String codigoExterno;
    private final double porcentajeComision;

    public CanalExterno(String codigoExterno) {
        this(codigoExterno, 0.15);
    }

    public CanalExterno(String codigoExterno, double porcentajeComision) {
        if (codigoExterno == null || codigoExterno.isBlank()) {
            throw new IllegalArgumentException("El código externo es obligatorio");
        }
        if (porcentajeComision < 0 || porcentajeComision > 1) {
            throw new IllegalArgumentException("El porcentaje de comisión debe estar entre 0 y 1");
        }
        this.codigoExterno = codigoExterno;
        this.porcentajeComision = porcentajeComision;
    }

    public String getCodigoExterno() {
        return codigoExterno;
    }

    @Override
    public boolean exigeAnticipo() {
        return false;
    }

    @Override
    public long calcularAnticipo(long valorEstancia) {
        return 0;
    }

    @Override
    public long calcularComision(long valorEstancia) {
        return Math.round(valorEstancia * porcentajeComision);
    }

    @Override
    public TipoCanal getTipo() {
        return TipoCanal.EXTERNA;
    }
}
