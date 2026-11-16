package com.example.proyect.viewController;

import com.example.proyect.App;
import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Alojamiento;
import com.example.proyect.model.Apartamento;
import com.example.proyect.model.CanalPortal;
import com.example.proyect.model.Huesped;
import com.example.proyect.model.Ocupante;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.Reserva;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HuespedViewController {

    // ----- Pestaña Disponibilidad -----
    @FXML private Label lblBienvenida;
    @FXML private Label lblMensaje;
    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private Spinner<Integer> spPersonas;
    @FXML private TableView<Apartamento> tblDisponibles;

    // ----- Pestaña Cotizar y reservar -----
    @FXML private ComboBox<Apartamento> cmbApartamento;
    @FXML private DatePicker dpEntradaR;
    @FXML private DatePicker dpSalidaR;
    @FXML private TextField txtNombreOcupante;
    @FXML private DatePicker dpNacimientoOcupante;
    @FXML private ListView<Ocupante> lstOcupantes;
    @FXML private Label lblMensajeReserva;
    @FXML private TextArea txtCotizacion;

    private final SistemaController sistema = SistemaController.getInstancia();
    private final ObservableList<Ocupante> ocupantes = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        Huesped titular = (Huesped) sistema.getUsuarioActual();
        lblBienvenida.setText("Huésped: " + titular.getNombre());

        // Disponibilidad
        spPersonas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
        dpEntrada.setValue(LocalDate.now().plusDays(1));
        dpSalida.setValue(LocalDate.now().plusDays(3));

        // Cotizar y reservar
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
                return a == null ? "" : a.getCodigo() + " - " + a.getNombre()
                        + " (cap. " + a.getCapacidad() + ")";
            }

            @Override
            public Apartamento fromString(String texto) {
                return null;
            }
        });
        dpEntradaR.setValue(LocalDate.now().plusDays(30));
        dpSalidaR.setValue(LocalDate.now().plusDays(32));

        // El titular entra como primer ocupante (se puede quitar)
        ocupantes.add(new Ocupante(titular.getNombre(), titular.getFechaNacimiento()));
        lstOcupantes.setItems(ocupantes);
        lstOcupantes.setCellFactory(lista -> new ListCell<Ocupante>() {
            @Override
            protected void updateItem(Ocupante o, boolean vacio) {
                super.updateItem(o, vacio);
                setText(vacio || o == null ? null : o.nombre() + " - nació el " + o.fechaNacimiento());
            }
        });
    }

    // ===================== Pestaña Disponibilidad =====================

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

    // ===================== Pestaña Cotizar y reservar =====================

    @FXML
    private void agregarOcupante() {
        String nombre = txtNombreOcupante.getText().trim();
        LocalDate nacimiento = dpNacimientoOcupante.getValue();
        if (nombre.isEmpty() || nacimiento == null) {
            lblMensajeReserva.setText("Escriba el nombre y la fecha de nacimiento del ocupante");
            return;
        }
        if (nacimiento.isAfter(LocalDate.now())) {
            lblMensajeReserva.setText("La fecha de nacimiento no puede ser futura");
            return;
        }
        ocupantes.add(new Ocupante(nombre, nacimiento));
        txtNombreOcupante.clear();
        dpNacimientoOcupante.setValue(null);
        lblMensajeReserva.setText("");
    }

    @FXML
    private void quitarOcupante() {
        Ocupante seleccionado = lstOcupantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblMensajeReserva.setText("Seleccione el ocupante que desea quitar");
            return;
        }
        ocupantes.remove(seleccionado);
        lblMensajeReserva.setText("");
    }

    @FXML
    private void cotizar() {
        lblMensajeReserva.setText("");
        Apartamento apto = cmbApartamento.getValue();
        if (apto == null) {
            lblMensajeReserva.setText("Seleccione un apartamento");
            return;
        }
        try {
            txtCotizacion.setText(generarCotizacion(apto, dpEntradaR.getValue(), dpSalidaR.getValue()));
        } catch (ReglaNegocioException ex) {
            txtCotizacion.clear();
            lblMensajeReserva.setText(ex.getMessage());
        }
    }

    @FXML
    private void crearReserva() {
        lblMensajeReserva.setText("");
        Apartamento apto = cmbApartamento.getValue();
        if (apto == null) {
            lblMensajeReserva.setText("Seleccione un apartamento");
            return;
        }
        Alojamiento alojamiento = sistema.getAlojamiento();
        LocalDate entrada = dpEntradaR.getValue();
        LocalDate salida = dpSalidaR.getValue();
        try {
            long valor = alojamiento.cotizarEstancia(apto.getCodigo(), entrada, salida, ocupantes);

            int consecutivo = alojamiento.getReservas().size() + 1;
            String codigo = String.format("RES-P%03d", consecutivo);
            while (alojamiento.buscarReserva(codigo) != null) {
                consecutivo++;
                codigo = String.format("RES-P%03d", consecutivo);
            }

            Huesped titular = (Huesped) sistema.getUsuarioActual();
            Reserva reserva = new Reserva(codigo, entrada, salida, valor, titular, apto,
                    new ArrayList<>(ocupantes), new CanalPortal());
            alojamiento.crearReserva(reserva);

            long anticipo = reserva.getCanal().calcularAnticipo(valor);
            lblMensajeReserva.setText("Reserva " + codigo + " creada en estado PENDIENTE. "
                    + "Pague el anticipo de " + dinero(anticipo) + " para confirmarla.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensajeReserva.setText(ex.getMessage());
        }
    }

    private String generarCotizacion(Apartamento apto, LocalDate entrada, LocalDate salida) {
        Alojamiento alojamiento = sistema.getAlojamiento();
        List<Long> desglose = alojamiento.desglosarEstancia(apto.getCodigo(), entrada, salida, ocupantes);

        int pagan = 0;
        for (Ocupante o : ocupantes) {
            if (o.calcularEdad(entrada) >= 6) {
                pagan++;
            }
        }

        StringBuilder texto = new StringBuilder();
        texto.append("Apartamento ").append(apto.getCodigo()).append(" - ").append(apto.getNombre()).append("\n");
        texto.append("Ocupantes: ").append(ocupantes.size())
                .append(" (pagan ").append(pagan).append(", los menores de 6 años no pagan)\n\n");

        long total = 0;
        for (int i = 0; i < desglose.size(); i++) {
            LocalDate noche = entrada.plusDays(i);
            texto.append(noche).append("   ").append(alojamiento.temporadaDe(noche))
                    .append("   ").append(dinero(desglose.get(i))).append("\n");
            total += desglose.get(i);
        }
        texto.append("\nTotal de la estancia: ").append(dinero(total)).append("\n");
        texto.append("Anticipo por Portal (30 %): ")
                .append(dinero(new CanalPortal().calcularAnticipo(total)));
        return texto.toString();
    }

    private static String dinero(long valor) {
        return String.format("$%,d", valor);
    }

    @FXML
    private void cerrarSesion() throws IOException {
        sistema.cerrarSesion();
        App.mostrar("login.fxml", 420, 340);
    }
}