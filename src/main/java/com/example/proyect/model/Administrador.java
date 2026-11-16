package com.example.proyect.model;


public class Administrador extends Usuario {

    public Administrador(String nombre, String documento, String correo, String telefono,
                         String usuario, String contrasena) {
        super(nombre, documento, correo, telefono, usuario, contrasena);
    }

    @Override
    public Rol getRol() {
        return Rol.ADMINISTRADOR;
    }
}
