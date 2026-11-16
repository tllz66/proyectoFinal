package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Alojamiento;
import com.example.proyect.model.Cargo;
import com.example.proyect.model.CargoServicio;
import com.example.proyect.model.EstadoReserva;
import com.example.proyect.model.MedioPago;
import com.example.proyect.model.Pago;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.Reserva;
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
import java.util.function.BooleanSupplier;

public class ReservasRecepcionViewController {

    @FXML private TableView<Reserva> tblReservas;
    @FXML private TableColumn<Reserva, String> colCodigo;
    @FXML private TableColumn<Reserva, String> colTitular;
    @FXML private TableColumn<Reserva, String> colApto;
    @FXML private TableColumn<Reserva, String> colEntrada;
    @FXML private TableColumn<Reserva, String> colSalida;
    @FXML private TableColumn<Reserva, String> colCanal;
    @FXML private TableColumn<Reserva, String> colEstado;
    @FXML private TableColumn<Reserva, String> colSaldo;
    @FXML private TextField txtMonto;
    @FXML private ComboBox<MedioPago> cmbMedio;
    @FXML private TextField txtTipoServicio;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtValorUnitario;
    @FXML private Label lblMensaje;
    @FXML private TextArea txtDetalle;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colTitular.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitular().getNombre()));
        colApto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApartamento().getCodigo()));
        colEntrada.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaEntrada().toString()));
        colSalida.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaSalida().toString()));
        colCanal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCanal().getTipo().toString()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().toString()));
        colSaldo.setCellValueFactory(c -> new SimpleStringProperty(dinero(c.getValue().getFolio().calcularSaldo())));

        cmbMedio.setItems(FXCollections.observableArrayList(MedioPago.values()));
        cmbMedio.setValue(MedioPago.EFECTIVO);

        tblReservas.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> mostrarDetalle());

        cargarReservas(null);
    }

    @FXML
    private void actualizar() {
        lblMensaje.setText("");
        cargarReservas(tblReservas.getSelectionModel().getSelectedItem());
    }

    // ===================== Estados de la reserva =====================

    @FXML
    private void confirmar() {
        ejecutar((a, r) -> a.confirmarReserva(r.getCodigo()), "Reserva confirmada.");
    }

    @FXML
    private void checkIn() {
        ejecutar((a, r) -> a.hacerCheckIn(r.getCodigo()),
                "Check-in registrado: la reserva está EN CURSO y el apartamento OCUPADO.");
    }

    @FXML
    private void checkOut() {
        ejecutar((a, r) -> a.hacerCheckOut(r.getCodigo()),
                "Check-out registrado: la reserva está FINALIZADA y el apartamento PENDIENTE DE PREPARACIÓN.");
    }

    @FXML
    private void noShow() {
        ejecutar((a, r) -> a.declararNoShow(r.getCodigo()),
                "No-show declarado. Se retiene el 100 % de lo pagado.");
    }

    @FXML
    private void cancelar() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        Alojamiento alojamiento = sistema.getAlojamiento();
        try {
            long devolucion = alojamiento.calcularDevolucion(r.getCodigo(), LocalDateTime.now());
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Cancelar la reserva " + r.getCodigo() + "?\nSe devolverían " + dinero(devolucion) + ".",
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

    // ===================== Folio =====================

    @FXML
    private void registrarPago() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        Long monto = leerEntero(txtMonto.getText());
        if (monto == null || monto <= 0) {
            lblMensaje.setText("Escriba el monto como un número entero mayor que cero, sin puntos ni comas");
            return;
        }
        if (!r.estaActiva()) {
            lblMensaje.setText("La reserva está " + r.getEstado() + " y ya no admite pagos");
            return;
        }
        if (monto > r.getFolio().calcularSaldo()) {
            lblMensaje.setText("El pago supera el saldo pendiente (" + dinero(r.getFolio().calcularSaldo()) + ")");
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
    private void agregarCargo() {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        String tipo = txtTipoServicio.getText().trim();
        Long cantidad = leerEntero(txtCantidad.getText());
        Long valor = leerEntero(txtValorUnitario.getText());
        if (tipo.isEmpty() || cantidad == null || valor == null || cantidad <= 0 || valor <= 0) {
            lblMensaje.setText("Escriba el servicio, la cantidad y el valor unitario (enteros mayores que cero)");
            return;
        }
        if (!r.estaActiva()) {
            lblMensaje.setText("La reserva está " + r.getEstado() + " y ya no admite cargos");
            return;
        }
        try {
            Cargo cargo = new CargoServicio(tipo + " x" + cantidad, LocalDate.now(), tipo,
                    cantidad.intValue(), valor);
            sistema.getAlojamiento().registrarCargo(r.getCodigo(), cargo);
            txtTipoServicio.clear();
            txtCantidad.clear();
            txtValorUnitario.clear();
            lblMensaje.setText("Cargo agregado al folio: " + dinero(cargo.calcularValor()));
            cargarReservas(r);
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    // ===================== Métodos privados =====================

    /** Operación sobre la reserva seleccionada: muestra el éxito o el motivo del rechazo. */
    private interface Operacion {
        boolean aplicar(Alojamiento alojamiento, Reserva reserva);
    }

    private void ejecutar(Operacion operacion, String mensajeExito) {
        Reserva r = seleccionada();
        if (r == null) {
            return;
        }
        try {
            BooleanSupplier llamada = () -> operacion.aplicar(sistema.getAlojamiento(), r);
            llamada.getAsBoolean();
            lblMensaje.setText(mensajeExito);
            cargarReservas(r);
        } catch (ReglaNegocioException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    private void cargarReservas(Reserva seleccionar) {
        List<Reserva> todas = sistema.getAlojamiento().getReservas();
        tblReservas.setItems(FXCollections.observableArrayList(todas));
        tblReservas.refresh();
        if (seleccionar != null && todas.contains(seleccionar)) {
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

    private Long leerEntero(String texto) {
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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
        t.append("Titular: ").append(r.getTitular().getNombre()).append(" (").append(r.getTitular().getDocumento())
                .append(")  -  ").append(r.getOcupantes().size()).append(" ocupante(s)\n");
        t.append("Apartamento ").append(r.getApartamento().getCodigo()).append(" - ")
                .append(r.getApartamento().getNombre()).append(" (").append(r.getApartamento().getEstado())
                .append("), ").append(r.calcularNoches()).append(" noche(s)\n");
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