package com.example.chessgame.controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class MenuController {

    @FXML
    private void handleVsAI(ActionEvent event) throws Exception {
        openGame(event, true);
    }

    @FXML
    private void handleTwoPlayer(ActionEvent event) throws Exception {
        openGame(event, false);
    }

    private void openGame(ActionEvent event, boolean vsAI) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/chessgame/hello-view.fxml")
        );

        Scene scene = new Scene(loader.load());

// 🔥 THIS LINE IS CRITICAL
        HelloController controller = loader.getController();
        controller.setVsAI(vsAI);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }
}