package com.example.chessgame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {


    public Move getRandomMove(String color) {

        List<Move> moves = new ArrayList<>();

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {

                Piece piece = board[fromRow][fromCol];

                if (piece != null && piece.getColor().equals(color)) {

                    Position from = new Position(fromRow, fromCol);

                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {

                            Position to = new Position(toRow, toCol);

                            if (isValidMove(from, to) &&
                                    isMoveSafe(from, to, color)) {

                                moves.add(new Move(from, to));
                            }
                        }
                    }
                }
            }
        }

        if (moves.isEmpty()) return null;

        Random rand = new Random();
        return moves.get(rand.nextInt(moves.size()));
    }
    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
        initialize();
    }

    private void initialize() {

        // Pawns
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn("BLACK", new Position(1, col));
            board[6][col] = new Pawn("WHITE", new Position(6, col));
        }

        // Knights
        board[0][1] = new Knight("BLACK", new Position(0, 1));
        board[0][6] = new Knight("BLACK", new Position(0, 6));

        board[7][1] = new Knight("WHITE", new Position(7, 1));
        board[7][6] = new Knight("WHITE", new Position(7, 6));

        // Rooks
        board[0][0] = new Rook("BLACK", new Position(0, 0));
        board[0][7] = new Rook("BLACK", new Position(0, 7));

        board[7][0] = new Rook("WHITE", new Position(7, 0));
        board[7][7] = new Rook("WHITE", new Position(7, 7));

        // Bishops
        board[0][2] = new Bishop("BLACK", new Position(0, 2));
        board[0][5] = new Bishop("BLACK", new Position(0, 5));

        board[7][2] = new Bishop("WHITE", new Position(7, 2));
        board[7][5] = new Bishop("WHITE", new Position(7, 5));

        // Queens
        board[0][3] = new Queen("BLACK", new Position(0, 3));
        board[7][3] = new Queen("WHITE", new Position(7, 3));

        // Kings
        board[0][4] = new King("BLACK", new Position(0, 4));
        board[7][4] = new King("WHITE", new Position(7, 4));
    }

    public Piece getPiece(int row, int col) {
        return board[row][col];
    }
    public void movePiece(Position from, Position to) {
        Piece piece = board[from.row][from.col];

        if (piece != null && isValidMove(from, to)) {

            board[to.row][to.col] = piece;
            board[from.row][from.col] = null;

            piece.position = to;

            // ⭐ mark as moved
            piece.hasMoved = true;
        }
    }
    public boolean isValidMove(Position from, Position to) {
        Piece piece = board[from.row][from.col];

        if (piece instanceof Pawn pawn) {
            return pawn.isValidMove(from, to, board);
        }

        if (piece instanceof Knight knight) {
            return knight.isValidMove(from, to, board);
        }
        if (piece instanceof Rook rook) {
            return rook.isValidMove(from, to, board);
        }
        if (piece instanceof Bishop bishop) {
            return bishop.isValidMove(from, to, board);
        }
        if (piece instanceof Queen queen) {
            return queen.isValidMove(from, to, board);
        }
        if (piece instanceof King king) {
            return king.isValidMove(from, to, board);
        }

        return false;
    }

    public boolean isKingInCheck(String color) {

        Position kingPosition = null;

        // 1. Find the king
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece piece = board[row][col];

                if (piece instanceof King &&
                        piece.getColor().equals(color)) {

                    kingPosition = new Position(row, col);
                }
            }
        }

        // 2. Check if any enemy can attack king
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece piece = board[row][col];

                if (piece != null &&
                        !piece.getColor().equals(color)) {

                    if (isValidMove(new Position(row, col), kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    public boolean isMoveSafe(Position from, Position to, String color) {

        Piece piece = board[from.row][from.col];
        Piece captured = board[to.row][to.col];

        // make move temporarily
        board[to.row][to.col] = piece;
        board[from.row][from.col] = null;

        Position oldPosition = piece.position;
        piece.position = to;

        // check if king is in danger
        boolean inCheck = isKingInCheck(color);

        // undo move
        board[from.row][from.col] = piece;
        board[to.row][to.col] = captured;
        piece.position = oldPosition;

        return !inCheck;
    }
    public boolean isCheckmate(String color) {

        // 1. If not in check → not checkmate
        if (!isKingInCheck(color)) {
            return false;
        }

        // 2. Try ALL possible moves
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {

                Piece piece = board[fromRow][fromCol];

                // only current player's pieces
                if (piece != null && piece.getColor().equals(color)) {

                    Position from = new Position(fromRow, fromCol);

                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {

                            Position to = new Position(toRow, toCol);

                            if (isValidMove(from, to) &&
                                    isMoveSafe(from, to, color)) {

                                // found at least one escape move
                                return false;
                            }
                        }
                    }
                }
            }
        }

        // no moves → checkmate
        return true;
    }
    public void promotePawn(Position pos) {

        Piece piece = board[pos.row][pos.col];

        if (piece instanceof Pawn) {

            // WHITE reaches top
            if (piece.getColor().equals("WHITE") && pos.row == 0) {
                board[pos.row][pos.col] = new Queen("WHITE", pos);
            }

            // BLACK reaches bottom
            if (piece.getColor().equals("BLACK") && pos.row == 7) {
                board[pos.row][pos.col] = new Queen("BLACK", pos);
            }
        }
    }
}