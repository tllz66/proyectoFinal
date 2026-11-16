package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.Alojamiento;
import com.example.proyect.model.Apartamento;
import com.example.proyect.model.ReglaNegocioException;
import com.example.proyect.model.Reserva;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;

import java.time.LocalDate;

public class ReportesAdminViewController {

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private TextArea txtReporte;

    private final Alojamiento aloj = SistemaController.getInstancia().getAlojamiento();

    @FXML
    private void initialize() {
        dpInicio.setValue(LocalDate.now().withDayOfMonth(1));
        dpFin.setValue(LocalDate.now().plusMonths(1).withDayOfMonth(1));
    }

    private boolean fechasValidas() {
        LocalDate i = dpInicio.getValue();
        LocalDate f = dpFin.getValue();
        if (i == null || f == null || !f.isAfter(i)) {
            txtReporte.setText("Seleccione un periodo válido (la fecha final debe ser posterior a la inicial).");
            return false;
        }
        return true;
    }

    @FXML
    private void ingresos() {
        if (!fechasValidas()) {
            return;
        }
        try {
            long total = aloj.calcularIngresosPorPeriodo(dpInicio.getValue(), dpFin.getValue());
            txtReporte.setText("Ingresos del periodo " + dpInicio.getValue() + " a " + dpFin.getValue()
                    + " (pagos menos devoluciones, sin descontar comisión externa):\n\n" + Util.dinero(total));
        } catch (ReglaNegocioException ex) {
            txtReporte.setText(ex.getMessage());
        }
    }

    @FXML
    private void ocupacion() {
        if (!fechasValidas()) {
            return;
        }
        try {
            StringBuilder t = new StringBuilder("Ocupación por apartamento:\n\n");
            for (String fila : aloj.reporteOcupacion(dpInicio.getValue(), dpFin.getValue())) {
                t.append(fila).append("\n");
            }
            txtReporte.setText(t.toString());
        } catch (ReglaNegocioException ex) {
            txtReporte.setText(ex.getMessage());
        }
    }

    @FXML
    private void porCanal() {
        StringBuilder t = new StringBuilder("Reservas por canal:\n\n");
        for (String fila : aloj.reporteReservasPorCanal()) {
            t.append(fila).append("\n");
        }
        txtReporte.setText(t.toString());
    }

    @FXML
    private void saldos() {
        StringBuilder t = new StringBuilder("Reservas con saldo pendiente:\n\n");
        for (Reserva r : aloj.reporteSaldosPendientes()) {
            t.append(r.getCodigo()).append(" | ").append(r.getTitular().getNombre()).append(" | ")
                    .append(r.getEstado()).append(" | saldo ")
                    .append(Util.dinero(r.getFolio().calcularSaldo())).append("\n");
        }
        txtReporte.setText(t.toString());
    }

    @FXML
    private void novedades() {
        StringBuilder t = new StringBuilder("Apartamentos con más novedades:\n\n");
        for (Apartamento a : aloj.reporteApartamentosConMasNovedades()) {
            t.append(a.getCodigo()).append(" - ").append(a.getNombre()).append(" | ")
                    .append(a.obtenerNovedades().size()).append(" novedad(es)\n");
        }
        txtReporte.setText(t.toString());
    }
}
