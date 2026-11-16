package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Apartamento;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.Temporada;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

public class ApartamentosAdminViewController {

    @FXML private TableView<Apartamento> tblApartamentos;
    @FXML private TableColumn<Apartamento, String> colCodigo;
    @FXML private TableColumn<Apartamento, String> colNombre;
    @FXML private TableColumn<Apartamento, String> colDormitorios;
    @FXML private TableColumn<Apartamento, String> colCapacidad;
    @FXML private TableColumn<Apartamento, String> colBaja;
    @FXML private TableColumn<Apartamento, String> colMedia;
    @FXML private TableColumn<Apartamento, String> colAlta;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDormitorios;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtCaracteristicas;
    @FXML private TextField txtBaja;
    @FXML private TextField txtMedia;
    @FXML private TextField txtAlta;
    @FXML private Label lblMensaje;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colDormitorios.setCellValueFactory(c -> new SimpleStringProperty("" + c.getValue().getDormitorios()));
        colCapacidad.setCellValueFactory(c -> new SimpleStringProperty("" + c.getValue().getCapacidad()));
        colBaja.setCellValueFactory(c -> new SimpleStringProperty(Util.dinero(c.getValue().getTarifa(Temporada.BAJA))));
        colMedia.setCellValueFactory(c -> new SimpleStringProperty(Util.dinero(c.getValue().getTarifa(Temporada.MEDIA))));
        colAlta.setCellValueFactory(c -> new SimpleStringProperty(Util.dinero(c.getValue().getTarifa(Temporada.ALTA))));
        tblApartamentos.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> {
            if (s != null) {
                txtCodigo.setText(s.getCodigo());
                txtNombre.setText(s.getNombre());
                txtDormitorios.setText("" + s.getDormitorios());
                txtCapacidad.setText("" + s.getCapacidad());
                txtCaracteristicas.setText(s.getCaracteristicas());
                txtBaja.setText("" + s.getTarifa(Temporada.BAJA));
                txtMedia.setText("" + s.getTarifa(Temporada.MEDIA));
                txtAlta.setText("" + s.getTarifa(Temporada.ALTA));
            }
        });
        actualizar();
    }

    private void actualizar() {
        List<Apartamento> activos = new ArrayList<>();
        for (Apartamento a : sistema.getAlojamiento().getApartamentos()) {
            if (a.isActivo()) {
                activos.add(a);
            }
        }
        tblApartamentos.setItems(FXCollections.observableArrayList(activos));
    }

    @FXML
    private void registrar() {
        Long dorm = Util.entero(txtDormitorios.getText());
        Long cap = Util.entero(txtCapacidad.getText());
        Long baja = Util.entero(txtBaja.getText());
        Long media = Util.entero(txtMedia.getText());
        Long alta = Util.entero(txtAlta.getText());
        if (dorm == null || cap == null || baja == null || media == null || alta == null) {
            lblMensaje.setText("Dormitorios, capacidad y tarifas deben ser números enteros");
            return;
        }
        try {
            sistema.getAlojamiento().registrarApartamento(new Apartamento(txtCodigo.getText().trim(),
                    txtNombre.getText().trim(), dorm.intValue(), cap.intValue(),
                    txtCaracteristicas.getText().trim(), baja, media, alta));
            lblMensaje.setText("Apartamento registrado.");
            actualizar();
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    @FXML
    private void modificar() {
        Apartamento sel = tblApartamentos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensaje.setText("Seleccione un apartamento de la tabla");
            return;
        }
        Long baja = Util.entero(txtBaja.getText());
        Long media = Util.entero(txtMedia.getText());
        Long alta = Util.entero(txtAlta.getText());
        if (baja == null || media == null || alta == null) {
            lblMensaje.setText("Las tarifas deben ser números enteros");
            return;
        }
        try {
            sistema.getAlojamiento().modificarApartamento(sel.getCodigo(), txtNombre.getText().trim(),
                    txtCaracteristicas.getText().trim(), baja, media, alta);
            lblMensaje.setText("Apartamento modificado (código, dormitorios y capacidad no cambian).");
            tblApartamentos.refresh();
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Apartamento sel = tblApartamentos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensaje.setText("Seleccione un apartamento de la tabla");
            return;
        }
        if (!Util.confirmar("¿Eliminar el apartamento " + sel.getCodigo() + "?")) {
            return;
        }
        try {
            sistema.getAlojamiento().eliminarApartamento(sel.getCodigo());
            lblMensaje.setText("Apartamento eliminado.");
            actualizar();
        } catch (ReglaNegocioException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }
}
