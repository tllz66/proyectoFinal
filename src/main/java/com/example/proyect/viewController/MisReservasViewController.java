package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Alojamiento;
import com.example.proyect.model.Cargo;
import com.example.proyect.model.EstadoReserva;
import com.example.proyect.model.MedioPago;
import com.example.proyect.model.Pago;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.Reserva;
import com.example.proyect.model.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MisReservasViewController {

    @FXML private TableView<Reserva> tblReservas;
    @FXML private TableColumn<Reserva, String> colCodigo;
    @FXML private TableColumn<Reserva, String> colApto;
    @FXML private TableColumn<Reserva, String> colEntrada;
    @FXML private TableColumn<Reserva, String> colSalida;
    @FXML private TableColumn<Reserva, String> colEstado;
    @FXML private TableColumn<Reserva, String> colValor;
    @FXML private TableColumn<Reserva, String> colSaldo;
    @FXML private TextField txtMonto;
    @FXML private ComboBox<MedioPago> cmbMedio;
    @FXML private Label lblMensaje;
    @FXML private TextArea txtDetalle;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colApto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApartamento().getCodigo()));
        colEntrada.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaEntrada().toString()));
        colSalida.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaSalida().toString()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().toString()));
        colValor.setCellValueFactory(c -> new SimpleStringProperty(dinero(c.getValue().getValorEstancia())));
        colSaldo.setCellValueFactory(c -> new SimpleStringProperty(dinero(c.getValue().getFolio().calcularSaldo())));

        cmbMedio.setItems(FXCollections.observableArrayList(MedioPago.values()));
        cmbMedio.setValue(MedioPago.TRANSFERENCIA);

        tblReservas.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> mostrarDetalle());

        cargarReservas(null);
    }

    @FXML
    private void actualizar() {
        lblMensaje.setText("");
        cargarReservas(tblReservas.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void registrarPago() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        long monto;
        try {
            monto = Long.parseLong(txtMonto.getText().trim());
        } catch (NumberFormatException e) {
            lblMensaje.setText("Escriba el monto como un número entero, sin puntos ni comas");
            return;
        }
        if (monto <= 0) {
            lblMensaje.setText("El monto debe ser mayor que cero");
            return;
        }
        if (!r.estaActiva()) {
            lblMensaje.setText("La reserva está " + r.getEstado() + " y ya no admite pagos");
            return;
        }
        if (monto > r.getFolio().calcularSaldo()) {
            lblMensaje.setText("El abono supera el saldo pendiente (" + dinero(r.getFolio().calcularSaldo()) + ")");
            return;
        }
        try {
            Pago pago = new Pago(monto, cmbMedio.getValue(), LocalDate.now());
            sistema.getAlojamiento().registrarPago(r.getCodigo(), pago);
            txtMonto.clear();
            lblMensaje.setText("Pago registrado. Estado de la reserva: " + r.getEstado());
            cargarReservas(r);
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    @FXML
    private void verDevolucion() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        try {
            long devolucion = sistema.getAlojamiento().calcularDevolucion(r.getCodigo(), LocalDateTime.now());
            lblMensaje.setText("Si cancela ahora se le devolverían " + dinero(devolucion)
                    + " de los " + dinero(r.getFolio().calcularTotalPagos()) + " pagados.");
        } catch (ReglaNegocioException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    @FXML
    private void cancelarReserva() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        Alojamiento alojamiento = sistema.getAlojamiento();
        try {
            long devolucion = alojamiento.calcularDevolucion(r.getCodigo(), LocalDateTime.now());
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Cancelar la reserva " + r.getCodigo() + "?\nSe le devolverían " + dinero(devolucion) + ".",
                    ButtonType.YES, ButtonType.NO);
            confirmacion.setHeaderText("Cancelación");
            Optional<ButtonType> respuesta = confirmacion.showAndWait();
            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                alojamiento.cancelarReserva(r.getCodigo());
                lblMensaje.setText("Reserva cancelada. Devolución registrada: " + dinero(devolucion));
                cargarReservas(r);
            }
        } catch (ReglaNegocioException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    // ===================== Métodos privados =====================

    private void cargarReservas(Reserva seleccionar) {
        Usuario usuario = sistema.getUsuarioActual();
        List<Reserva> mias = sistema.getAlojamiento().consultarReservasDeHuesped(usuario.getDocumento());
        tblReservas.setItems(FXCollections.observableArrayList(mias));
        tblReservas.refresh();
        if (seleccionar != null && mias.contains(seleccionar)) {
            tblReservas.getSelectionModel().select(seleccionar);
        }
        mostrarDetalle();
    }

    private Reserva seleccionada() {
        Reserva r = tblReservas.getSelectionModel().getSelectedItem();
        if (r == null) {
            lblMensaje.setText("Seleccione una reserva de la tabla");
        }
        return r;
    }

    private void mostrarDetalle() {
        Reserva r = tblReservas.getSelectionModel().getSelectedItem();
        if (r == null) {
            txtDetalle.clear();
            return;
        }
        StringBuilder t = new StringBuilder();
        t.append("Reserva ").append(r.getCodigo()).append(" - ").append(r.getEstado())
                .append(" (canal ").append(r.getCanal().getTipo()).append(")\n");
        t.append("Apartamento ").append(r.getApartamento().getCodigo()).append(" - ")
                .append(r.getApartamento().getNombre()).append(", ")
                .append(r.calcularNoches()).append(" noche(s)\n");
        if (r.getEstado() == EstadoReserva.PENDIENTE && r.getCanal().exigeAnticipo()) {
            t.append("Anticipo requerido para confirmar: ")
                    .append(dinero(r.getCanal().calcularAnticipo(r.getValorEstancia()))).append("\n");
        }

        t.append("\nCARGOS\n");
        for (Cargo c : r.getFolio().getCargos()) {
            t.append("  ").append(c.getFecha()).append("  ").append(c.getDescripcion())
                    .append("  ").append(dinero(c.calcularValor())).append("\n");
        }
        t.append("\nPAGOS\n");
        if (r.getFolio().getPagos().isEmpty()) {
            t.append("  (sin pagos)\n");
        }
        for (Pago p : r.getFolio().getPagos()) {
            t.append("  ").append(p.fecha()).append("  ").append(p.medio())
                    .append("  ").append(dinero(p.monto()))
                    .append(p.esDevolucion() ? "  (devolución)" : "").append("\n");
        }
        t.append("\nTotal cargos: ").append(dinero(r.getFolio().calcularTotalCargos()));
        t.append("\nTotal pagos: ").append(dinero(r.getFolio().calcularTotalPagos()));
        t.append("\nSaldo: ").append(dinero(r.getFolio().calcularSaldo()));
        txtDetalle.setText(t.toString());
    }

    private static String dinero(long valor) {
        return String.format("$%,d", valor);
    }
}