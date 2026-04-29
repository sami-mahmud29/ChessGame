package com.example.chessgame.logic;

import com.example.chessgame.model.*;

public class AiMode implements GameMode {

    @Override
    public boolean handleMove(Board board, Position from, Position to, String currentTurn) {

        if (board.isValidMove(from, to) &&
                board.isMoveSafe(from, to, currentTurn)) {

            board.movePiece(from, to);
            board.promotePawn(to);
            return true;
        }

        return false;
    }

    @Override
    public String getNextTurn(String currentTurn) {
        return currentTurn.equals("WHITE") ? "BLACK" : "WHITE";
    }

    @Override
    public void afterMove(Board board, String currentTurn) {

        // 🤖 AI plays BLACK
        if (currentTurn.equals("BLACK")) {

            Move aiMove = board.getRandomMove("BLACK");

            if (aiMove != null) {
                board.movePiece(aiMove.from, aiMove.to);
                board.promotePawn(aiMove.to);
            }
        }
    }
}