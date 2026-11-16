package com.example.proyect.viewController;

import com.example.proyect.App;
import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Apartamento;
import com.example.proyect.model.ReglaNegocioException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class HuespedViewController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblMensaje;
    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private Spinner<Integer> spPersonas;
    @FXML private TableView<Apartamento> tblDisponibles;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        lblBienvenida.setText("Huésped: " + sistema.getUsuarioActual().getNombre());
        spPersonas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
        dpEntrada.setValue(LocalDate.now().plusDays(1));
        dpSalida.setValue(LocalDate.now().plusDays(3));
    }

    @FXML
    private void consultarDisponibles() {
        try {
            List<Apartamento> disponibles = sistema.getAlojamiento().consultarDisponibles(
                    dpEntrada.getValue(), dpSalida.getValue(), spPersonas.getValue());
            tblDisponibles.setItems(FXCollections.observableArrayList(disponibles));
            if (disponibles.isEmpty()) {
                lblMensaje.setText("No hay apartamentos disponibles para esas fechas y esa cantidad de personas.");
            } else {
                lblMensaje.setText(disponibles.size() + " apartamento(s) disponible(s).");
            }
        } catch (ReglaNegocioException ex) {
            tblDisponibles.getItems().clear();
            lblMensaje.setText(ex.getMessage());
        }
    }

    @FXML
    private void cerrarSesion() throws IOException {
        sistema.cerrarSesion();
        App.mostrar("login.fxml", 420, 340);
    }
}