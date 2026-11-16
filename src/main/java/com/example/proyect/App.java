package com.example.proyect;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage ventana;

    @Override
    public void start(Stage stage) throws IOException {
        ventana = stage;
        stage.setTitle("SGA - Aparta-Suites UQ");
        mostrar("login.fxml", 420, 340);
        stage.show();
    }

    /** Cambia la pantalla de la ventana. El .fxml está en resources/com/example/proyect. */
    public static void mostrar(String fxml, double ancho, double alto) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        ventana.setScene(new Scene(loader.load(), ancho, alto));
        ventana.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }
}