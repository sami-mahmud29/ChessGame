package com.example.chessgame;

import com.example.chessgame.model.Board;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // ✅ TEST CHESS BOARD LOGIC
        Board board = new Board();

        if (board.getPiece(6, 0) != null) {
            System.out.println("White pawn is placed correctly!");
        } else {
            System.out.println("Something is wrong!");
        }

        // ✅ LOAD UI
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("main-menu.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        stage.setTitle("Chess Game");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}