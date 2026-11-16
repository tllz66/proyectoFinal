package com.example.proyect.model;

import java.time.LocalDate;

public class Huesped extends Usuario {

    private final LocalDate fechaNacimiento;
    private String foto; // opcional

    public Huesped(String nombre, String documento, String correo, String telefono,
                   String usuario, String contrasena, LocalDate fechaNacimiento) {
        super(nombre, documento, correo, telefono, usuario, contrasena);
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no es válida");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    @Override
    public Rol getRol() {
        return Rol.HUESPED;
    }
}
