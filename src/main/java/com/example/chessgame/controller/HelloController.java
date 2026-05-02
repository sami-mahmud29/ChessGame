package com.example.chessgame.controller;

import com.example.chessgame.logic.LanConnection;
import com.example.chessgame.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class HelloController {

    @FXML
    private GridPane boardGrid;

    @FXML
    private Label timerLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button startGameButton;

    @FXML
    private Button quitButton;

    // 🎮 GAME MODE
    private boolean vsAI = false;
    private boolean lanMode = false;
    private boolean lanHost = false;
    private String lanHostAddress = "127.0.0.1";
    private int lanPort = 5000;
    private String localColor = "WHITE";
    private boolean lanConnected = false;
    private LanConnection lanConnection;

    public void setVsAI(boolean vsAI) {
        this.vsAI = vsAI;
    }

    public void enableLanMode(boolean isHost, String hostAddress, int port) {
        lanMode = true;
        lanHost = isHost;
        lanHostAddress = hostAddress;
        lanPort = port;
        localColor = lanHost ? "WHITE" : "BLACK";
        vsAI = false;
    }

    private Board board;
    private Position selectedPosition = null;

    private String currentTurn = "WHITE";
    private boolean gameStarted = false;
    private boolean aiThinking = false;

    // ⏱ CHESS CLOCK
    private int whiteTime = 600;
    private int blackTime = 600;

    private Timeline timeline;
    private PauseTransition aiPause;

    @FXML
    public void initialize() {
        board = new Board();
        drawBoard();
    }

    // ▶️ START BUTTON
    @FXML
    private void handleStart() {

        if (gameStarted) return; // prevent restart spam

        if (lanMode) {
            startLanSession();
            return;
        }

        gameStarted = true;
        startLocalTimers();
        statusLabel.setText("Game Started! Turn: WHITE");

        // Hide Start after game begins (but keep Quit).
        if (startGameButton != null) {
            startGameButton.setVisible(false);
            startGameButton.setDisable(true);
        }

        if (timeline != null) timeline.stop();

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateClock())
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        // Stop timers so no AI "delayed move" runs after leaving.
        if (timeline != null) timeline.stop();
        if (aiPause != null) aiPause.stop();
        if (lanConnection != null) lanConnection.close();

        aiThinking = false;
        lanConnected = false;
        gameStarted = false;
        currentTurn = "WHITE";
        selectedPosition = null;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/chessgame/main-menu.fxml")
            );
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("Failed to return to main menu", e);
        }
    }

    // ⏱ CLOCK
    private void updateClock() {

        if (currentTurn.equals("WHITE")) {
            whiteTime--;
        } else {
            blackTime--;
        }

        updateTimerDisplay();

        if (whiteTime <= 0) {
            timeline.stop();
            statusLabel.setText("BLACK WINS (Time)");
        }

        if (blackTime <= 0) {
            timeline.stop();
            statusLabel.setText("WHITE WINS (Time)");
        }
    }

    private void updateTimerDisplay() {
        timerLabel.setText("White: " + formatTime(whiteTime) +
                " | Black: " + formatTime(blackTime));
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    // 🎯 DRAW BOARD
    private void drawBoard() {
        boardGrid.getChildren().clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Button cell = new Button();
                cell.setPrefSize(75, 75);

                String baseStyle = (row + col) % 2 == 0
                        ? "-fx-background-color: #f0d9b5;"
                        : "-fx-background-color: #b58863;";

                // selected
                if (selectedPosition != null &&
                        selectedPosition.row == row &&
                        selectedPosition.col == col) {
                    baseStyle = "-fx-background-color: gold;";
                }
                // possible move
                else if (selectedPosition != null &&
                        board.isValidMove(selectedPosition, new Position(row, col))) {
                    baseStyle = "-fx-background-color: lightgreen;";
                }

                cell.setStyle(baseStyle + " -fx-font-size: 28;");

                int r = row;
                int c = col;
                cell.setOnAction(e -> handleClick(r, c));

                Piece piece = board.getPiece(row, col);

                if (piece != null) {
                    if (piece instanceof Pawn)
                        cell.setText(piece.getColor().equals("WHITE") ? "♙" : "♟");
                    else if (piece instanceof Knight)
                        cell.setText(piece.getColor().equals("WHITE") ? "♘" : "♞");
                    else if (piece instanceof Rook)
                        cell.setText(piece.getColor().equals("WHITE") ? "♖" : "♜");
                    else if (piece instanceof Bishop)
                        cell.setText(piece.getColor().equals("WHITE") ? "♗" : "♝");
                    else if (piece instanceof Queen)
                        cell.setText(piece.getColor().equals("WHITE") ? "♕" : "♛");
                    else if (piece instanceof King)
                        cell.setText(piece.getColor().equals("WHITE") ? "♔" : "♚");
                }

                boardGrid.add(cell, col, row);
            }
        }
    }

    // 🎮 HANDLE CLICK
    private void handleClick(int row, int col) {

        if (!gameStarted) {
            statusLabel.setText(lanMode ? "Press Start and connect LAN first." : "Press Start first!");
            return;
        }

        if (lanMode && !lanConnected) {
            statusLabel.setText("LAN not connected yet.");
            return;
        }

        if (lanMode && !currentTurn.equals(localColor)) {
            statusLabel.setText("Waiting for opponent move...");
            selectedPosition = null;
            return;
        }

        if (vsAI && currentTurn.equals("BLACK")) {
            statusLabel.setText(aiThinking ? "AI is thinking..." : "AI turn");
            selectedPosition = null; // ignore any selection made during AI turn
            return;
        }

        // SELECT
        if (selectedPosition == null) {

            Piece piece = board.getPiece(row, col);

            if (piece != null && piece.getColor().equals(currentTurn)) {
                selectedPosition = new Position(row, col);
            }

            drawBoard();
            return;
        }

        // MOVE
        Position target = new Position(row, col);

        Piece destinationPiece = board.getPiece(target.row, target.col);

        if (!(destinationPiece instanceof King) &&
                board.isValidMove(selectedPosition, target) &&
                board.isMoveSafe(selectedPosition, target, currentTurn)) {

            // ✅ PLAYER MOVE
            board.movePiece(selectedPosition, target);
            board.promotePawn(target);

            // 🔥 CHECKMATE (after move)
            if (board.isCheckmate(currentTurn.equals("WHITE") ? "BLACK" : "WHITE")) {
                timeline.stop();
                statusLabel.setText("CHECKMATE! " +
                        (currentTurn.equals("WHITE") ? "WHITE WINS" : "BLACK WINS"));
                statusLabel.setStyle("-fx-font-size: 20; -fx-text-fill: red;");
                drawBoard();
                selectedPosition = null;
                return;
            }

            // 🔄 SWITCH TURN
            currentTurn = currentTurn.equals("WHITE") ? "BLACK" : "WHITE";

            drawBoard();

            if (lanMode) {
                sendLanMove(selectedPosition, target);
                statusLabel.setText((board.isKingInCheck(currentTurn) ? "CHECK! " : "") +
                        "Waiting for opponent...");
            }

            // 🤖 AI MOVE
            if (!lanMode && vsAI && currentTurn.equals("BLACK")) {
                aiThinking = true;
                statusLabel.setText((board.isKingInCheck(currentTurn) ? "CHECK! " : "") +
                        "AI is thinking...");

                aiPause = new PauseTransition(Duration.seconds(0.5));

                aiPause.setOnFinished(e -> {

                    Move aiMove = board.getRandomMove("BLACK");

                    if (aiMove != null) {
                        board.movePiece(aiMove.from, aiMove.to);
                        board.promotePawn(aiMove.to);
                    }

                    // 🔥 CHECKMATE after AI
                    if (board.isCheckmate("WHITE")) {
                        timeline.stop();
                        statusLabel.setText("CHECKMATE! BLACK WINS");
                        statusLabel.setStyle("-fx-font-size: 20; -fx-text-fill: red;");
                        drawBoard();
                        return;
                    }

                    // 🔄 BACK TO PLAYER
                    currentTurn = "WHITE";
                    aiThinking = false;

                    statusLabel.setText((board.isKingInCheck(currentTurn) ? "CHECK! " : "") +
                            "Turn: " + currentTurn);

                    drawBoard();
                });

                aiPause.play();

            } else {
                // 2 PLAYER
                if (!lanMode) {
                    statusLabel.setText((board.isKingInCheck(currentTurn) ? "CHECK! " : "") +
                            "Turn: " + currentTurn);
                }
            }

        } else {
            statusLabel.setText("Illegal move!");
        }

        selectedPosition = null;
        drawBoard();
    }

    private void startLocalTimers() {
        whiteTime = 600;
        blackTime = 600;
        currentTurn = "WHITE";
        updateTimerDisplay();
        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void startLanSession() {
        statusLabel.setText(lanHost
                ? "Waiting for player on port " + lanPort + "..."
                : "Connecting to " + lanHostAddress + ":" + lanPort + "...");
        startGameButton.setDisable(true);

        Thread connectThread = new Thread(() -> {
            try {
                lanConnection = new LanConnection(lanHost, lanHostAddress, lanPort);
                lanConnection.connect();

                Platform.runLater(() -> {
                    lanConnected = true;
                    gameStarted = true;
                    startLocalTimers();
                    if (startGameButton != null) {
                        startGameButton.setVisible(false);
                        startGameButton.setDisable(true);
                    }
                    statusLabel.setText("Connected! You are " + localColor + ". Turn: " + currentTurn);
                });

                lanConnection.listen(this::applyOpponentMove, () -> Platform.runLater(() -> {
                    lanConnected = false;
                    if (gameStarted) {
                        statusLabel.setText("Opponent disconnected.");
                    }
                }));
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    lanConnected = false;
                    startGameButton.setDisable(false);
                    statusLabel.setText("LAN connection failed. Check host IP/port, host pressed Start, and firewall.");
                });
            }
        });
        connectThread.setDaemon(true);
        connectThread.start();
    }

    private void sendLanMove(Position from, Position to) {
        if (!lanConnected || lanConnection == null) return;
        try {
            lanConnection.sendMove(new Move(from, to));
        } catch (IOException ex) {
            lanConnected = false;
            statusLabel.setText("Failed to send move. Connection lost.");
        }
    }

    private void applyOpponentMove(Move move) {
        Platform.runLater(() -> {
            if (!lanConnected) return;

            Piece destinationPiece = board.getPiece(move.to.row, move.to.col);
            if (destinationPiece instanceof King) {
                return;
            }

            if (!board.isValidMove(move.from, move.to) ||
                    !board.isMoveSafe(move.from, move.to, currentTurn)) {
                statusLabel.setText("Received invalid move from opponent.");
                return;
            }

            board.movePiece(move.from, move.to);
            board.promotePawn(move.to);

            if (board.isCheckmate(currentTurn.equals("WHITE") ? "BLACK" : "WHITE")) {
                timeline.stop();
                statusLabel.setText("CHECKMATE! " +
                        (currentTurn.equals("WHITE") ? "WHITE WINS" : "BLACK WINS"));
                statusLabel.setStyle("-fx-font-size: 20; -fx-text-fill: red;");
                drawBoard();
                selectedPosition = null;
                return;
            }

            currentTurn = currentTurn.equals("WHITE") ? "BLACK" : "WHITE";
            statusLabel.setText((board.isKingInCheck(currentTurn) ? "CHECK! " : "") +
                    "Turn: " + currentTurn);
            selectedPosition = null;
            drawBoard();
        });
    }
}