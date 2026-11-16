package com.example.proyect.viewController;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/** Utilidades compartidas por las pantallas. */
public final class Util {

    private Util() {
    }

    public static String dinero(long valor) {
        return String.format("$%,d", valor);
    }

    /** Convierte el texto en un entero, o devuelve null si no es un número entero válido. */
    public static Long entero(String texto) {
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setHeaderText("Confirmación");
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.YES;
    }

    public static void informar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
