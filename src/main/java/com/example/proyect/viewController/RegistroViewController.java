package com.example.proyect.viewController;

import com.example.proyect.App;
import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Huesped;
import com.example.proyect.model.ReglaNegocioException;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegistroViewController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private DatePicker dpNacimiento;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblMensaje;

    @FXML
    private void crearCuenta() {
        if (dpNacimiento.getValue() == null) {
            lblMensaje.setText("Seleccione la fecha de nacimiento");
            return;
        }
        try {
            Huesped nuevo = new Huesped(txtNombre.getText().trim(), txtDocumento.getText().trim(),
                    txtCorreo.getText().trim(), txtTelefono.getText().trim(),
                    txtUsuario.getText().trim(), txtContrasena.getText(), dpNacimiento.getValue());
            SistemaController.getInstancia().getAlojamiento().registrarUsuario(nuevo);
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
            return;
        }
        Util.informar("Cuenta creada. Ya puede iniciar sesión con su usuario.");
        volver();
    }

    @FXML
    private void volver() {
        try {
            App.mostrar("login.fxml", 420, 340);
        } catch (IOException e) {
            lblMensaje.setText("No se pudo abrir la pantalla: " + e.getMessage());
        }
    }
}
