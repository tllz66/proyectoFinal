package com.example.proyect.model;


public class CanalDirecto implements CanalReserva {

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
        return 0;
    }

    @Override
    public TipoCanal getTipo() {
        return TipoCanal.DIRECTA;
    }
}
