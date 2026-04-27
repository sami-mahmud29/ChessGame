package com.example.chessgame.model;

public class Bishop extends Piece {

    public Bishop(String color, Position position) {
        super(color, position);
    }

    public boolean isValidMove(Position from, Position to, Piece[][] board) {

        int rowDiff = Math.abs(to.row - from.row);
        int colDiff = Math.abs(to.col - from.col);

        // must move diagonally
        if (rowDiff != colDiff) {
            return false;
        }

        int rowStep = (to.row > from.row) ? 1 : -1;
        int colStep = (to.col > from.col) ? 1 : -1;

        int r = from.row + rowStep;
        int c = from.col + colStep;

        // check path blocking
        while (r != to.row && c != to.col) {
            if (board[r][c] != null) {
                return false; // blocked
            }
            r += rowStep;
            c += colStep;
        }

        // destination: empty or enemy
        if (board[to.row][to.col] == null ||
                !board[to.row][to.col].getColor().equals(this.color)) {
            return true;
        }

        return false;
    }
}