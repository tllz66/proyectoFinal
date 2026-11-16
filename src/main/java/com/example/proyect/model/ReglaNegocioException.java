package com.example.proyect.model;

/** Se lanza cuando una operación incumple una regla del negocio. El mensaje explica el motivo. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
