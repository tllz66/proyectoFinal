package com.example.proyect.model;

public interface PoliticaCancelacion {

    /** Devuelve cuánto se le reembolsa al huésped (nunca negativo). */
    long calcularReembolso(long valorEstancia, long totalPagado, long horasAntelacion);
}
