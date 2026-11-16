package com.example.proyect.viewController;

import com.example.proyect.controller.SistemaController;
import com.example.proyect.model.PoliticaCancelacion;
import com.example.proyect.model.PoliticaEscalonada;
import com.example.proyect.model.ReglaNegocioException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PoliticaAdminViewController {

    @FXML private Label lblVigente;
    @FXML private TextField txtHorasTotal;
    @FXML private TextField txtHorasParcial;
    @FXML private TextField txtPorcentaje;
    @FXML private Label lblMensaje;

    private final SistemaController sistema = SistemaController.getInstancia();

    @FXML
    private void initialize() {
        mostrarVigente();
    }

    private void mostrarVigente() {
        PoliticaCancelacion p = sistema.getAlojamiento().getPoliticaVigente();
        if (p instanceof PoliticaEscalonada e) {
            lblVigente.setText("Vigente: reembolso total con " + e.getHorasReembolsoTotal()
                    + " h o más de antelación; entre " + e.getHorasRetencionParcial() + " h y "
                    + e.getHorasReembolsoTotal() + " h se retiene el "
                    + Math.round(e.getPorcentajeRetencionParcial() * 100) + " %; con menos de "
                    + e.getHorasRetencionParcial() + " h no hay reembolso.");
            txtHorasTotal.setText("" + e.getHorasReembolsoTotal());
            txtHorasParcial.setText("" + e.getHorasRetencionParcial());
            txtPorcentaje.setText("" + Math.round(e.getPorcentajeRetencionParcial() * 100));
        } else {
            lblVigente.setText("Vigente: " + p);
        }
    }

    @FXML
    private void guardar() {
        Long total = Util.entero(txtHorasTotal.getText());
        Long parcial = Util.entero(txtHorasParcial.getText());
        Long pct = Util.entero(txtPorcentaje.getText());
        if (total == null || parcial == null || pct == null) {
            lblMensaje.setText("Los tres valores deben ser números enteros (el porcentaje de 0 a 100)");
            return;
        }
        try {
            sistema.getAlojamiento().configurarPolitica(new PoliticaEscalonada(total, parcial, pct / 100.0));
            lblMensaje.setText("Política actualizada. Solo aplica a las reservas que se creen desde ahora.");
            mostrarVigente();
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            lblMensaje.setText(ex.getMessage());
        }
    }
}
