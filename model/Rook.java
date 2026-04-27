package com.example.chessgame.model;

public class Rook extends Piece {

    public Rook(String color, Position position) {
        super(color, position);
    }

    public boolean isValidMove(Position from, Position to, Piece[][] board) {

        // must move in straight line
        if (from.row != to.row && from.col != to.col) {
            return false;
        }

        // moving horizontally
        if (from.row == to.row) {
            int step = (to.col > from.col) ? 1 : -1;

            for (int c = from.col + step; c != to.col; c += step) {
                if (board[from.row][c] != null) {
                    return false; // blocked
                }
            }
        }

        // moving vertically
        if (from.col == to.col) {
            int step = (to.row > from.row) ? 1 : -1;

            for (int r = from.row + step; r != to.row; r += step) {
                if (board[r][from.col] != null) {
                    return false; // blocked
                }
            }
        }

        // destination check (empty or enemy)
        if (board[to.row][to.col] == null ||
                !board[to.row][to.col].getColor().equals(this.color)) {
            return true;
        }

        return false;
    }
}