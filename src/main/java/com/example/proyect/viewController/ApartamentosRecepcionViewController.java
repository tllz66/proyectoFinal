package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Apartamento;
import com.example.proyect.model.EstadoApartamento;
import com.example.proyect.model.Novedad;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.TipoNovedad;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApartamentosRecepcionViewController {

    @FXML private TableView<Apartamento> tblApartamentos;
    @FXML private TableColumn<Apartamento, String> colCodigo;
    @FXML private TableColumn<Apartamento, String> colNombre;
    @FXML private TableColumn<Apartamento, String> colEstado;
    @FXML private TableColumn<Apartamento, String> colMotivo;
    @FXML private ComboBox<EstadoApartamento> cmbEstado;
    @FXML private TextField txtMotivo;
    @FXML private ComboBox<TipoNovedad> cmbTipoNovedad;
    @FXML private TextField txtDescripcion;
    @FXML private ListView<String> lstNovedades;
    @FXML private Label lblMensaje;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().toString()));
        colMotivo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMotivoFueraServicio() == null ? "" : c.getValue().getMotivoFueraServicio()));
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoApartamento.values()));
        cmbTipoNovedad.setItems(FXCollections.observableArrayList(TipoNovedad.values()));
        tblApartamentos.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> mostrarNovedades());
        actualizar();
    }

    private void actualizar() {
        Apartamento sel = tblApartamentos.getSelectionModel().getSelectedItem();
        List<Apartamento> activos = new ArrayList<>();
        for (Apartamento a : sistema.getAlojamiento().getApartamentos()) {
            if (a.isActivo()) {
                activos.add(a);
            }
        }
        tblApartamentos.setItems(FXCollections.observableArrayList(activos));
        if (sel != null) {
            tblApartamentos.getSelectionModel().select(sel);
        }
        mostrarNovedades();
    }

    private void mostrarNovedades() {
        List<String> filas = new ArrayList<>();
        Apartamento a = tblApartamentos.getSelectionModel().getSelectedItem();
        if (a != null) {
            for (Novedad n : a.obtenerNovedades()) {
                filas.add(n.fecha().toLocalDate() + " | " + n.tipo() + " | " + n.descripcion());
            }
        }
        lstNovedades.setItems(FXCollections.observableArrayList(filas));
    }

    private Apartamento seleccionado() {
        Apartamento a = tblApartamentos.getSelectionModel().getSelectedItem();
        if (a == null) {
            lblMensaje.setText("Seleccione un apartamento de la tabla");
        }
        return a;
    }

    @FXML
    private void cambiarEstado() {
        Apartamento a = seleccionado();
        if (a == null) {
            return;
        }
        if (cmbEstado.getValue() == null) {
            lblMensaje.setText("Seleccione el nuevo estado");
            return;
        }
        try {
            sistema.getAlojamiento().cambiarEstadoApartamento(a.getCodigo(), cmbEstado.getValue(),
                    txtMotivo.getText().trim());
            lblMensaje.setText("Estado actualizado.");
        } catch (ReglaNegocioException | IllegalArgumentException | IllegalStateException ex) {
            lblMensaje.setText(ex.getMessage());
        }
        actualizar();
        tblApartamentos.refresh();
    }

    @FXML
    private void registrarNovedad() {
        Apartamento a = seleccionado();
        if (a == null) {
            return;
        }
        if (cmbTipoNovedad.getValue() == null) {
            lblMensaje.setText("Seleccione el tipo de novedad");
            return;
        }
        try {
            sistema.getAlojamiento().registrarNovedad(a.getCodigo(),
                    new Novedad(LocalDateTime.now(), cmbTipoNovedad.getValue(), txtDescripcion.getText().trim()));
            txtDescripcion.clear();
            lblMensaje.setText("Novedad registrada.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
        mostrarNovedades();
    }
}
