package com.example.proyect.controller;

import com.example.proyect.model.Alojamiento;
import com.example.proyect.model.DatosPrueba;
import com.example.proyect.model.Usuario;

/**
 * Une las pantallas con el modelo. Hay una sola instancia en toda la aplicación,
 * así todas las pantallas comparten el mismo Alojamiento y el mismo usuario con sesión.
 */
public class SistemaController {

    private static SistemaController instancia;

    private final Alojamiento alojamiento;
    private Usuario usuarioActual;

    private SistemaController() {
        this.alojamiento = DatosPrueba.crearAlojamientoConDatos();
    }

    public static SistemaController getInstancia() {
        if (instancia == null) {
            instancia = new SistemaController();
        }
        return instancia;
    }

    /** Devuelve el usuario si las credenciales son correctas, o null si no. */
    public Usuario iniciarSesion(String usuario, String contrasena) {
        usuarioActual = alojamiento.iniciarSesion(usuario, contrasena);
        return usuarioActual;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }

    public Alojamiento getAlojamiento() {
        return alojamiento;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}