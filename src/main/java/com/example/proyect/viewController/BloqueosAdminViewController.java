package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Apartamento;
import com.example.proyect.model.Bloqueo;
import com.example.proyect.model.ReglaNegocioException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

public class BloqueosAdminViewController {

    @FXML private ComboBox<Apartamento> cmbApartamento;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private TextField txtMotivo;
    @FXML private ListView<String> lstBloqueos;
    @FXML private Label lblMensaje;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        List<Apartamento> activos = new ArrayList<>();
        for (Apartamento a : sistema.getAlojamiento().getApartamentos()) {
            if (a.isActivo()) {
                activos.add(a);
            }
        }
        cmbApartamento.setItems(FXCollections.observableArrayList(activos));
        cmbApartamento.setConverter(new StringConverter<Apartamento>() {
            @Override
            public String toString(Apartamento a) {
                return a == null ? "" : a.getCodigo() + " - " + a.getNombre();
            }

            @Override
            public Apartamento fromString(String texto) {
                return null;
            }
        });
        actualizar();
    }

    private void actualizar() {
        List<String> filas = new ArrayList<>();
        for (Bloqueo b : sistema.getAlojamiento().getBloqueos()) {
            filas.add(b.codigoApartamento() + " | " + b.fechaInicio() + " a " + b.fechaFin() + " | " + b.motivo());
        }
        lstBloqueos.setItems(FXCollections.observableArrayList(filas));
    }

    @FXML
    private void registrar() {
        if (cmbApartamento.getValue() == null || dpInicio.getValue() == null || dpFin.getValue() == null) {
            lblMensaje.setText("Seleccione el apartamento y las dos fechas");
            return;
        }
        try {
            sistema.getAlojamiento().registrarBloqueo(new Bloqueo(cmbApartamento.getValue().getCodigo(),
                    dpInicio.getValue(), dpFin.getValue(), txtMotivo.getText().trim()));
            lblMensaje.setText("Bloqueo registrado.");
            actualizar();
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }
}
