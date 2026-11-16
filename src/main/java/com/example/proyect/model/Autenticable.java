package com.example.proyect.model;


public interface Autenticable {

    boolean autenticar(String usuario, String contrasena);

    Rol getRol();
}
