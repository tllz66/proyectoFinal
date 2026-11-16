module com.example.proyect {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.proyect to javafx.fxml;
    exports com.example.proyect;

    opens com.example.proyect.viewController to javafx.fxml;
    exports com.example.proyect.viewController;

    //opens com.example.proyect.controller to javafx.fxml;
    //exports com.example.proyect.controller;
}