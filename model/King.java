package com.example.chessgame.model;

public class King extends Piece {

    public King(String color, Position position) {
        super(color, position);
    }

    public boolean isValidMove(Position from, Position to, Piece[][] board) {

        int rowDiff = Math.abs(to.row - from.row);
        int colDiff = Math.abs(to.col - from.col);

        // king moves 1 step in any direction
        if (rowDiff <= 1 && colDiff <= 1) {

            // destination must be empty OR enemy
            if (board[to.row][to.col] == null ||
                    !board[to.row][to.col].getColor().equals(this.color)) {
                return true;
            }
        }

        return false;
    }
}