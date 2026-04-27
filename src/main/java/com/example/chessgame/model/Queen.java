package com.example.chessgame.model;

public class Queen extends Piece {

    public Queen(String color, Position position) {
        super(color, position);
    }

    public boolean isValidMove(Position from, Position to, Piece[][] board) {

        int rowDiff = Math.abs(to.row - from.row);
        int colDiff = Math.abs(to.col - from.col);

        // ---------- ROOK-LIKE MOVEMENT ----------
        if (from.row == to.row || from.col == to.col) {

            // horizontal
            if (from.row == to.row) {
                int step = (to.col > from.col) ? 1 : -1;

                for (int c = from.col + step; c != to.col; c += step) {
                    if (board[from.row][c] != null) {
                        return false;
                    }
                }
            }

            // vertical
            if (from.col == to.col) {
                int step = (to.row > from.row) ? 1 : -1;

                for (int r = from.row + step; r != to.row; r += step) {
                    if (board[r][from.col] != null) {
                        return false;
                    }
                }
            }
        }

        // ---------- BISHOP-LIKE MOVEMENT ----------
        else if (rowDiff == colDiff) {

            int rowStep = (to.row > from.row) ? 1 : -1;
            int colStep = (to.col > from.col) ? 1 : -1;

            int r = from.row + rowStep;
            int c = from.col + colStep;

            while (r != to.row && c != to.col) {
                if (board[r][c] != null) {
                    return false;
                }
                r += rowStep;
                c += colStep;
            }
        }

        // ❌ not valid pattern
        else {
            return false;
        }

        // destination check (empty or enemy)
        if (board[to.row][to.col] == null ||
                !board[to.row][to.col].getColor().equals(this.color)) {
            return true;
        }

        return false;
    }
}