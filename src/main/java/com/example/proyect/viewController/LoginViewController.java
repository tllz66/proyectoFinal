package com.example.proyect.viewController;

import com.example.proyect.model.Rol;
import com.example.proyect.App;
import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginViewController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblMensaje;

    @FXML
    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            lblMensaje.setText("Ingrese usuario y contraseña");
            return;
        }

        Usuario autenticado = SistemaController.getInstancia().iniciarSesion(usuario, contrasena);
        if (autenticado == null) {
            lblMensaje.setText("Usuario o contraseña incorrectos");
            txtContrasena.clear();
            return;
        }

        try {
            if (autenticado.getRol() == Rol.HUESPED) {
                App.mostrar("huesped.fxml", 900, 560);
            } else {
                App.mostrar("principal.fxml", 720, 460);
            }
        } catch (IOException e) {
            lblMensaje.setText("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }
}