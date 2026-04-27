package com.example.chessgame.model;

public class Knight extends Piece {

    public Knight(String color, Position position) {
        super(color, position);
    }

    public boolean isValidMove(Position from, Position to, Piece[][] board) {

        int rowDiff = Math.abs(to.row - from.row);
        int colDiff = Math.abs(to.col - from.col);

        // L-shape move
        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {

            // allow move if empty OR enemy piece
            if (board[to.row][to.col] == null ||
                    !board[to.row][to.col].getColor().equals(this.color)) {
                return true;
            }
        }

        return false;
    }
}