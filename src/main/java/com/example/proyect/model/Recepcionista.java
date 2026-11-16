package com.example.proyect.model;

import java.time.LocalDate;

public class Recepcionista extends Usuario {

    private final LocalDate fechaContratacion;

    public Recepcionista(String nombre, String documento, String correo, String telefono,
                         String usuario, String contrasena, LocalDate fechaContratacion) {
        super(nombre, documento, correo, telefono, usuario, contrasena);
        if (fechaContratacion == null) {
            throw new IllegalArgumentException("La fecha de contratación es obligatoria");
        }
        this.fechaContratacion = fechaContratacion;
    }

    public LocalDate getFechaContratacion() { return fechaContratacion; }

    @Override
    public Rol getRol() {
        return Rol.RECEPCIONISTA;
    }
}
