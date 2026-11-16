package com.example.proyect.viewController;

import com.example.proyect.App;
import com.example.proyect.controller.SistemaController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class AdministradorViewController {

    @FXML private Label lblBienvenida;

    @FXML
    private void initialize() {
        lblBienvenida.setText("Administrador: " + SistemaController.getInstancia().getUsuarioActual().getNombre());
    }

    @FXML
    private void cerrarSesion() throws IOException {
        SistemaController.getInstancia().cerrarSesion();
        App.mostrar("login.fxml", 420, 340);
    }
}
