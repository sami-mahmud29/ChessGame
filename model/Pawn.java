package com.example.chessgame.model;

public class Pawn extends Piece {

    public Pawn(String color, Position position) {
        super(color, position);
    }

    public boolean isValidMove(Position from, Position to, Piece[][] board) {

        int rowDiff = to.row - from.row;
        int colDiff = to.col - from.col;

        // WHITE pawn
        if (color.equals("WHITE")) {

            // 1 step forward
            if (colDiff == 0 && rowDiff == -1 && board[to.row][to.col] == null) {
                return true;
            }

            // ⭐ 2 steps forward (first move only)
            if (!hasMoved && colDiff == 0 && rowDiff == -2 &&
                    board[from.row - 1][from.col] == null &&
                    board[to.row][to.col] == null) {
                return true;
            }

            // capture
            if (Math.abs(colDiff) == 1 && rowDiff == -1 &&
                    board[to.row][to.col] != null &&
                    !board[to.row][to.col].getColor().equals("WHITE")) {
                return true;
            }
        }

        // BLACK pawn
        if (color.equals("BLACK")) {

            // 1 step forward
            if (colDiff == 0 && rowDiff == 1 && board[to.row][to.col] == null) {
                return true;
            }

            // ⭐ 2 steps forward
            if (!hasMoved && colDiff == 0 && rowDiff == 2 &&
                    board[from.row + 1][from.col] == null &&
                    board[to.row][to.col] == null) {
                return true;
            }

            // capture
            if (Math.abs(colDiff) == 1 && rowDiff == 1 &&
                    board[to.row][to.col] != null &&
                    !board[to.row][to.col].getColor().equals("BLACK")) {
                return true;
            }
        }

        return false;
    }
}