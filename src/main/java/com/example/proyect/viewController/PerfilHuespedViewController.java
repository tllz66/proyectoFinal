package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Huesped;
import com.example.proyect.model.ReglaNegocioException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class PerfilHuespedViewController {

    @FXML private Label lblDocumento;
    @FXML private Label lblUsuario;
    @FXML private Label lblNacimiento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtFoto;
    @FXML private Label lblMensaje;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        Huesped h = (Huesped) sistema.getUsuarioActual();
        lblDocumento.setText(h.getDocumento());
        lblUsuario.setText(h.getUsuario());
        lblNacimiento.setText(h.getFechaNacimiento().toString());
        txtNombre.setText(h.getNombre());
        txtCorreo.setText(h.getCorreo());
        txtTelefono.setText(h.getTelefono());
        txtFoto.setText(h.getFoto() == null ? "" : h.getFoto());
    }

    @FXML
    private void elegirFoto() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccione su fotografía");
        File archivo = selector.showOpenDialog(txtFoto.getScene().getWindow());
        if (archivo != null) {
            txtFoto.setText(archivo.getAbsolutePath());
        }
    }

    @FXML
    private void guardar() {
        Huesped h = (Huesped) sistema.getUsuarioActual();
        try {
            sistema.getAlojamiento().editarUsuario(h.getDocumento(), txtNombre.getText().trim(),
                    txtCorreo.getText().trim(), txtTelefono.getText().trim());
            String foto = txtFoto.getText().trim();
            h.setFoto(foto.isEmpty() ? null : foto);
            lblMensaje.setText("Perfil actualizado.");
        } catch (ReglaNegocioException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }
}
