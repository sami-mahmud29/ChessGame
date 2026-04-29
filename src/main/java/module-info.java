module com.example.chessgame {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.chessgame to javafx.fxml;
    exports com.example.chessgame;
    exports com.example.chessgame.controller;
    opens com.example.chessgame.controller to javafx.fxml;
}