package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.PeriodoTemporada;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.Temporada;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.List;

public class TemporadasAdminViewController {

    @FXML private ComboBox<Temporada> cmbTemporada;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private ListView<String> lstPeriodos;
    @FXML private Label lblMensaje;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        cmbTemporada.setItems(FXCollections.observableArrayList(Temporada.MEDIA, Temporada.ALTA));
        actualizar();
    }

    private void actualizar() {
        List<String> filas = new ArrayList<>();
        for (PeriodoTemporada p : sistema.getAlojamiento().getTemporadas()) {
            filas.add(p.temporada() + ": " + p.fechaInicio() + " a " + p.fechaFin());
        }
        lstPeriodos.setItems(FXCollections.observableArrayList(filas));
    }

    @FXML
    private void definir() {
        if (cmbTemporada.getValue() == null || dpInicio.getValue() == null || dpFin.getValue() == null) {
            lblMensaje.setText("Seleccione la temporada y las dos fechas");
            return;
        }
        try {
            sistema.getAlojamiento().definirTemporada(
                    new PeriodoTemporada(cmbTemporada.getValue(), dpInicio.getValue(), dpFin.getValue()));
            lblMensaje.setText("Periodo registrado. Todo lo demás se cobra como temporada baja.");
            actualizar();
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }
}
