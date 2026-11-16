package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.Recepcionista;
import com.example.proyect.model.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

public class RecepcionistasAdminViewController {

    @FXML private TableView<Recepcionista> tblRecepcionistas;
    @FXML private TableColumn<Recepcionista, String> colDocumento;
    @FXML private TableColumn<Recepcionista, String> colNombre;
    @FXML private TableColumn<Recepcionista, String> colCorreo;
    @FXML private TableColumn<Recepcionista, String> colTelefono;
    @FXML private TableColumn<Recepcionista, String> colUsuario;
    @FXML private TableColumn<Recepcionista, String> colContratacion;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private DatePicker dpContratacion;
    @FXML private Label lblMensaje;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        colDocumento.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDocumento()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorreo()));
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsuario()));
        colContratacion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaContratacion().toString()));
        tblRecepcionistas.getSelectionModel().selectedItemProperty().addListener((o, a, r) -> {
            if (r != null) {
                txtNombre.setText(r.getNombre());
                txtDocumento.setText(r.getDocumento());
                txtCorreo.setText(r.getCorreo());
                txtTelefono.setText(r.getTelefono());
            }
        });
        actualizar();
    }

    private void actualizar() {
        List<Recepcionista> lista = new ArrayList<>();
        for (Usuario u : sistema.getAlojamiento().getUsuarios()) {
            if (u instanceof Recepcionista r && r.isActivo()) {
                lista.add(r);
            }
        }
        tblRecepcionistas.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    private void registrar() {
        if (dpContratacion.getValue() == null) {
            lblMensaje.setText("Seleccione la fecha de contratación");
            return;
        }
        try {
            sistema.getAlojamiento().registrarUsuario(new Recepcionista(txtNombre.getText().trim(),
                    txtDocumento.getText().trim(), txtCorreo.getText().trim(), txtTelefono.getText().trim(),
                    txtUsuario.getText().trim(), txtContrasena.getText(), dpContratacion.getValue()));
            lblMensaje.setText("Recepcionista registrado.");
            actualizar();
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    @FXML
    private void editar() {
        Recepcionista r = tblRecepcionistas.getSelectionModel().getSelectedItem();
        if (r == null) {
            lblMensaje.setText("Seleccione un recepcionista de la tabla");
            return;
        }
        try {
            sistema.getAlojamiento().editarUsuario(r.getDocumento(), txtNombre.getText().trim(),
                    txtCorreo.getText().trim(), txtTelefono.getText().trim());
            lblMensaje.setText("Datos actualizados (el documento no se puede cambiar).");
            tblRecepcionistas.refresh();
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    @FXML
    private void desvincular() {
        Recepcionista r = tblRecepcionistas.getSelectionModel().getSelectedItem();
        if (r == null) {
            lblMensaje.setText("Seleccione un recepcionista de la tabla");
            return;
        }
        if (!Util.confirmar("¿Desvincular a " + r.getNombre() + "?")) {
            return;
        }
        try {
            sistema.getAlojamiento().desvincularUsuario(r.getDocumento());
            lblMensaje.setText("Recepcionista desvinculado.");
            actualizar();
        } catch (ReglaNegocioException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }
}
