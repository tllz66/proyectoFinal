package com.example.proyect.viewController;

import com.example.proyect.App;
import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class PrincipalViewController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;

    @FXML
    private void initialize() {
        Usuario usuario = SistemaController.getInstancia().getUsuarioActual();
        lblBienvenida.setText("Bienvenido, " + usuario.getNombre());
        lblRol.setText("Rol: " + usuario.getRol());
    }

    @FXML
    private void cerrarSesion() throws IOException {
        SistemaController.getInstancia().cerrarSesion();
        App.mostrar("login.fxml", 420, 340);
    }
}