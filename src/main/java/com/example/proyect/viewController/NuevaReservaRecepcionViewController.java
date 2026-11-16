package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Alojamiento;
import com.example.proyect.model.Apartamento;
import com.example.proyect.model.CanalDirecto;
import com.example.proyect.model.CanalExterno;
import com.example.proyect.model.CanalReserva;
import com.example.proyect.model.ConflictoCanal;
import com.example.proyect.model.Huesped;
import com.example.proyect.model.Ocupante;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.Reserva;
import com.example.proyect.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NuevaReservaRecepcionViewController {

    @FXML private ComboBox<String> cmbCanal;
    @FXML private TextField txtCodigoExterno;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombreH;
    @FXML private TextField txtCorreoH;
    @FXML private TextField txtTelefonoH;
    @FXML private DatePicker dpNacimientoH;
    @FXML private Label lblHuesped;
    @FXML private ComboBox<Apartamento> cmbApartamento;
    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private TextField txtNombreOcupante;
    @FXML private DatePicker dpNacimientoOcupante;
    @FXML private ListView<Ocupante> lstOcupantes;
    @FXML private Label lblMensaje;
    @FXML private TextArea txtCotizacion;
    @FXML private ListView<String> lstConflictos;

    private final SistemaController sistema = SistemaController.getInstancia();
    private final ObservableList<Ocupante> ocupantes = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cmbCanal.setItems(FXCollections.observableArrayList("DIRECTA", "EXTERNA"));
        cmbCanal.setValue("DIRECTA");
        txtCodigoExterno.setDisable(true);
        cmbCanal.setOnAction(e -> txtCodigoExterno.setDisable(!"EXTERNA".equals(cmbCanal.getValue())));

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
                return a == null ? "" : a.getCodigo() + " - " + a.getNombre() + " (cap. " + a.getCapacidad() + ")";
            }

            @Override
            public Apartamento fromString(String texto) {
                return null;
            }
        });
        dpEntrada.setValue(LocalDate.now().plusDays(10));
        dpSalida.setValue(LocalDate.now().plusDays(12));

        lstOcupantes.setItems(ocupantes);
        lstOcupantes.setCellFactory(lista -> new ListCell<Ocupante>() {
            @Override
            protected void updateItem(Ocupante o, boolean vacio) {
                super.updateItem(o, vacio);
                setText(vacio || o == null ? null : o.nombre() + " - nació el " + o.fechaNacimiento());
            }
        });
        refrescarConflictos();
    }

    @FXML
    private void buscarHuesped() {
        Usuario u = sistema.getAlojamiento().buscarUsuario(txtDocumento.getText().trim());
        if (u instanceof Huesped h) {
            lblHuesped.setText("Huésped encontrado: " + h.getNombre());
            txtNombreH.setText(h.getNombre());
            txtCorreoH.setText(h.getCorreo());
            txtTelefonoH.setText(h.getTelefono());
            dpNacimientoH.setValue(h.getFechaNacimiento());
        } else if (u != null) {
            lblHuesped.setText("Ese documento pertenece a un usuario que no es huésped.");
        } else {
            lblHuesped.setText("No existe. Complete los datos y pulse «Registrar huésped».");
        }
    }

    @FXML
    private void registrarHuesped() {
        String doc = txtDocumento.getText().trim();
        if (dpNacimientoH.getValue() == null) {
            lblHuesped.setText("Seleccione la fecha de nacimiento del huésped");
            return;
        }
        try {
            // Usuario y contraseña iniciales = documento (el huésped puede cambiarlos luego)
            Huesped h = new Huesped(txtNombreH.getText().trim(), doc, txtCorreoH.getText().trim(),
                    txtTelefonoH.getText().trim(), doc, doc, dpNacimientoH.getValue());
            sistema.getAlojamiento().registrarUsuario(h);
            lblHuesped.setText("Huésped registrado. Usuario y contraseña iniciales: su documento.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblHuesped.setText(ex.getMessage());
        }
    }

    @FXML
    private void agregarOcupante() {
        String nombre = txtNombreOcupante.getText().trim();
        LocalDate nacimiento = dpNacimientoOcupante.getValue();
        if (nombre.isEmpty() || nacimiento == null) {
            lblMensaje.setText("Escriba el nombre y la fecha de nacimiento del ocupante");
            return;
        }
        if (nacimiento.isAfter(LocalDate.now())) {
            lblMensaje.setText("La fecha de nacimiento no puede ser futura");
            return;
        }
        ocupantes.add(new Ocupante(nombre, nacimiento));
        txtNombreOcupante.clear();
        dpNacimientoOcupante.setValue(null);
        lblMensaje.setText("");
    }

    @FXML
    private void quitarOcupante() {
        Ocupante o = lstOcupantes.getSelectionModel().getSelectedItem();
        if (o != null) {
            ocupantes.remove(o);
        }
    }

    @FXML
    private void cotizar() {
        lblMensaje.setText("");
        Apartamento apto = cmbApartamento.getValue();
        if (apto == null) {
            lblMensaje.setText("Seleccione un apartamento");
            return;
        }
        try {
            Alojamiento aloj = sistema.getAlojamiento();
            List<Long> desglose = aloj.desglosarEstancia(apto.getCodigo(), dpEntrada.getValue(),
                    dpSalida.getValue(), ocupantes);
            StringBuilder t = new StringBuilder();
            long total = 0;
            for (int i = 0; i < desglose.size(); i++) {
                LocalDate noche = dpEntrada.getValue().plusDays(i);
                t.append(noche).append("   ").append(aloj.temporadaDe(noche)).append("   ")
                        .append(Util.dinero(desglose.get(i))).append("\n");
                total += desglose.get(i);
            }
            t.append("\nTotal de la estancia: ").append(Util.dinero(total));
            txtCotizacion.setText(t.toString());
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }

    @FXML
    private void crearReserva() {
        lblMensaje.setText("");
        Apartamento apto = cmbApartamento.getValue();
        Usuario u = sistema.getAlojamiento().buscarUsuario(txtDocumento.getText().trim());
        if (apto == null) {
            lblMensaje.setText("Seleccione un apartamento");
            return;
        }
        if (!(u instanceof Huesped titular)) {
            lblMensaje.setText("Busque o registre primero al huésped titular");
            return;
        }
        Alojamiento aloj = sistema.getAlojamiento();
        boolean externa = "EXTERNA".equals(cmbCanal.getValue());
        try {
            long valor = aloj.cotizarEstancia(apto.getCodigo(), dpEntrada.getValue(), dpSalida.getValue(), ocupantes);
            CanalReserva canal;
            String codigo;
            if (externa) {
                String ext = txtCodigoExterno.getText().trim();
                if (ext.isEmpty()) {
                    lblMensaje.setText("Escriba el código externo de la reserva");
                    return;
                }
                canal = new CanalExterno(ext);
                codigo = "RES-EXT-" + ext;
            } else {
                canal = new CanalDirecto();
                int n = aloj.getReservas().size() + 1;
                codigo = String.format("RES-D%03d", n);
                while (aloj.buscarReserva(codigo) != null) {
                    n++;
                    codigo = String.format("RES-D%03d", n);
                }
            }
            Reserva r = new Reserva(codigo, dpEntrada.getValue(), dpSalida.getValue(), valor,
                    titular, apto, new ArrayList<>(ocupantes), canal);
            if (externa) {
                aloj.recibirReservaExterna(r);
            } else {
                aloj.crearReserva(r);
            }
            lblMensaje.setText("Reserva " + codigo + " creada en estado PENDIENTE por " + Util.dinero(valor)
                    + ". Confírmela en la pestaña Reservas.");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
        refrescarConflictos();
    }

    private void refrescarConflictos() {
        List<String> filas = new ArrayList<>();
        for (ConflictoCanal c : sistema.getAlojamiento().getConflictos()) {
            filas.add(c.fechaRegistro().toLocalDate() + " | " + c.codigoExterno() + " | " + c.codigoApartamento()
                    + " | " + c.fechaEntrada() + " a " + c.fechaSalida() + " | " + c.motivo());
        }
        lstConflictos.setItems(FXCollections.observableArrayList(filas));
    }
}
