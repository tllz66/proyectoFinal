package com.example.proyect.model;


public interface CanalReserva {

    boolean exigeAnticipo();

    long calcularAnticipo(long valorEstancia);

    long calcularComision(long valorEstancia);

    TipoCanal getTipo();
}
