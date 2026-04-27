package com.example.chessgame;

import com.example.chessgame.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class HelloController {

    @FXML
    private GridPane boardGrid;

    @FXML
    private Label timerLabel;

    @FXML
    private Label statusLabel;

    private Board board;
    private Position selectedPosition = null;

    private String currentTurn = "WHITE";
    private boolean gameStarted = false;

    private int whiteTime = 600; // 10 minutes
    private int blackTime = 600;

    private Timeline timeline;

    @FXML
    public void initialize() {
        board = new Board();
        drawBoard();
    }

    // ▶️ START BUTTON
    @FXML
    private void handleStart() {

        gameStarted = true;

        whiteTime = 600;
        blackTime = 600;

        updateTimerDisplay();

        statusLabel.setText("Game Started! Turn: WHITE");

        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateClock())
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    private void updateClock() {

        if (currentTurn.equals("WHITE")) {
            whiteTime--;
        } else {
            blackTime--;
        }

        updateTimerDisplay();

        // ⛔ time over
        if (whiteTime <= 0) {
            timeline.stop();
            statusLabel.setText("BLACK wins on time!");
        }

        if (blackTime <= 0) {
            timeline.stop();
            statusLabel.setText("WHITE wins on time!");
        }
    }
    private void updateTimerDisplay() {

        String whiteFormatted = formatTime(whiteTime);
        String blackFormatted = formatTime(blackTime);

        timerLabel.setText("White: " + whiteFormatted + " | Black: " + blackFormatted);
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }
    // 🎯 DRAW BOARD
    private void drawBoard() {
        boardGrid.getChildren().clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Button cell = new Button();
                cell.setPrefSize(75, 75);

                // 🎨 base color
                String baseStyle;
                if ((row + col) % 2 == 0) {
                    baseStyle = "-fx-background-color: #f0d9b5;";
                } else {
                    baseStyle = "-fx-background-color: #b58863;";
                }

                // 🟡 selected
                if (selectedPosition != null &&
                        selectedPosition.row == row &&
                        selectedPosition.col == col) {

                    baseStyle = "-fx-background-color: gold;";
                }

                // 🟢 possible move
                else if (selectedPosition != null &&
                        board.isValidMove(selectedPosition, new Position(row, col))) {

                    baseStyle = "-fx-background-color: lightgreen;";
                }

                baseStyle += " -fx-font-size: 28; -fx-border-color: transparent;";
                cell.setStyle(baseStyle);

                String finalBaseStyle = baseStyle;

                // hover
                cell.setOnMouseEntered(e ->
                        cell.setStyle(finalBaseStyle + " -fx-opacity: 0.8;")
                );

                cell.setOnMouseExited(e ->
                        cell.setStyle(finalBaseStyle)
                );

                int r = row;
                int c = col;

                cell.setOnAction(e -> handleClick(r, c));

                // ♟️ piece display
                Piece piece = board.getPiece(row, col);

                if (piece != null) {

                    if (piece instanceof Pawn) {
                        cell.setText(piece.getColor().equals("WHITE") ? "♙" : "♟");
                    } else if (piece instanceof Knight) {
                        cell.setText(piece.getColor().equals("WHITE") ? "♘" : "♞");
                    } else if (piece instanceof Rook) {
                        cell.setText(piece.getColor().equals("WHITE") ? "♖" : "♜");
                    } else if (piece instanceof Bishop) {
                        cell.setText(piece.getColor().equals("WHITE") ? "♗" : "♝");
                    } else if (piece instanceof Queen) {
                        cell.setText(piece.getColor().equals("WHITE") ? "♕" : "♛");
                    } else if (piece instanceof King) {
                        cell.setText(piece.getColor().equals("WHITE") ? "♔" : "♚");
                    }
                }

                boardGrid.add(cell, col, row);
            }
        }
    }

    // 🎮 HANDLE CLICK
    private void handleClick(int row, int col) {

        // ❌ block if not started
        if (!gameStarted) {
            statusLabel.setText("Press Start first!");
            return;
        }

        if (selectedPosition == null) {

            Piece piece = board.getPiece(row, col);

            if (piece != null && piece.getColor().equals(currentTurn)) {
                selectedPosition = new Position(row, col);
            }

        } else {

            Position target = new Position(row, col);

            if (board.isValidMove(selectedPosition, target) &&
                    board.isMoveSafe(selectedPosition, target, currentTurn)) {

                board.movePiece(selectedPosition, target);
                board.promotePawn(target);


                // 🔥 CHECK / CHECKMATE
                if (board.isCheckmate(currentTurn)) {
                    timeline.stop();
                    statusLabel.setText(currentTurn + " is in CHECKMATE!");
                    statusLabel.setStyle("-fx-font-size: 20; -fx-text-fill: red;");
                } else if (board.isKingInCheck(currentTurn)) {
                    statusLabel.setText(currentTurn + " is in CHECK!");
                    statusLabel.setStyle("-fx-font-size: 18; -fx-text-fill: orange;");
                }

                // 🔄 switch turn
                currentTurn = currentTurn.equals("WHITE") ? "BLACK" : "WHITE";

                if (!board.isCheckmate(currentTurn)) {
                    statusLabel.setText("Turn: " + currentTurn);
                    statusLabel.setStyle("-fx-font-size: 18; -fx-text-fill: black;");
                }

            } else {
                statusLabel.setText("Illegal move!");
            }

            selectedPosition = null;
        }

        drawBoard();
    }
}