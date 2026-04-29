package com.example.chessgame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {

    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
        initialize();
    }

    // ================= INIT =================
    private void initialize() {

        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn("BLACK", new Position(1, col));
            board[6][col] = new Pawn("WHITE", new Position(6, col));
        }

        board[0][1] = new Knight("BLACK", new Position(0, 1));
        board[0][6] = new Knight("BLACK", new Position(0, 6));
        board[7][1] = new Knight("WHITE", new Position(7, 1));
        board[7][6] = new Knight("WHITE", new Position(7, 6));

        board[0][0] = new Rook("BLACK", new Position(0, 0));
        board[0][7] = new Rook("BLACK", new Position(0, 7));
        board[7][0] = new Rook("WHITE", new Position(7, 0));
        board[7][7] = new Rook("WHITE", new Position(7, 7));

        board[0][2] = new Bishop("BLACK", new Position(0, 2));
        board[0][5] = new Bishop("BLACK", new Position(0, 5));
        board[7][2] = new Bishop("WHITE", new Position(7, 2));
        board[7][5] = new Bishop("WHITE", new Position(7, 5));

        board[0][3] = new Queen("BLACK", new Position(0, 3));
        board[7][3] = new Queen("WHITE", new Position(7, 3));

        board[0][4] = new King("BLACK", new Position(0, 4));
        board[7][4] = new King("WHITE", new Position(7, 4));
    }

    // ================= BASIC =================
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public void movePiece(Position from, Position to) {

        Piece piece = board[from.row][from.col];

        if (piece != null && isValidMove(from, to)) {

            board[to.row][to.col] = piece;
            board[from.row][from.col] = null;

            piece.position = to;
            piece.hasMoved = true;
        }
    }

    // ================= MOVE VALIDATION =================
    public boolean isValidMove(Position from, Position to) {
        if (from == null || to == null) return false;
        if (!isInsideBoard(from) || !isInsideBoard(to)) return false;
        if (from.row == to.row && from.col == to.col) return false;

        Piece piece = board[from.row][from.col];
        if (piece == null) return false;
        Piece destination = board[to.row][to.col];

        if (piece instanceof Pawn pawn)
            return pawn.isValidMove(from, to, board);

        if (piece instanceof Knight knight)
            return knight.isValidMove(from, to, board);

        if (piece instanceof Rook rook)
            return rook.isValidMove(from, to, board);

        if (piece instanceof Bishop bishop)
            return bishop.isValidMove(from, to, board);

        if (piece instanceof Queen queen)
            return queen.isValidMove(from, to, board);

        if (piece instanceof King king)
            return king.isValidMove(from, to, board);

        return false;
    }

    private boolean isInsideBoard(Position position) {
        return position.row >= 0 && position.row < 8 &&
                position.col >= 0 && position.col < 8;
    }

    // ================= AI =================
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

                            // Don't pick king-capture moves for the AI.
                            if (board[to.row][to.col] instanceof King) continue;

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

        return moves.get(new Random().nextInt(moves.size()));
    }

    // ================= CHECK SYSTEM =================
    public boolean isKingInCheck(String color) {

        Position kingPosition = null;

        // find king
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece piece = board[row][col];

                if (piece instanceof King &&
                        piece.getColor().equals(color)) {

                    kingPosition = new Position(row, col);
                    break;
                }
            }
        }

        if (kingPosition == null) return false;

        // enemy attack
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
        // In real chess you never "capture the king"; check/checkmate are detected instead.
        // Returning false prevents king-capture moves from being considered legal.
        if (board[to.row][to.col] instanceof King) {
            return false;
        }

        Piece movedPiece = board[from.row][from.col];
        Piece capturedPiece = board[to.row][to.col];

        // 🧪 simulate
        board[to.row][to.col] = movedPiece;
        board[from.row][from.col] = null;

        // 🔥 IMPORTANT: update position
        Position oldPosition = movedPiece.position;
        movedPiece.position = to;

        boolean inCheck = isKingInCheck(color);

        // 🔄 restore
        board[from.row][from.col] = movedPiece;
        board[to.row][to.col] = capturedPiece;

        // 🔥 restore position
        movedPiece.position = oldPosition;

        return !inCheck;
    }

    public boolean isCheckmate(String color) {

        if (!isKingInCheck(color)) return false;

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {

                Piece piece = board[fromRow][fromCol];

                if (piece != null && piece.getColor().equals(color)) {

                    Position from = new Position(fromRow, fromCol);

                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {

                            Position to = new Position(toRow, toCol);

                            // Never treat capturing the king as a legal "escape" move.
                            if (board[to.row][to.col] instanceof King) continue;

                            if (isValidMove(from, to) &&
                                    isMoveSafe(from, to, color)) {

                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    // ================= PROMOTION =================
    public void promotePawn(Position pos) {

        Piece piece = board[pos.row][pos.col];

        if (piece instanceof Pawn) {

            if (piece.getColor().equals("WHITE") && pos.row == 0) {
                board[pos.row][pos.col] = new Queen("WHITE", pos);
            }

            if (piece.getColor().equals("BLACK") && pos.row == 7) {
                board[pos.row][pos.col] = new Queen("BLACK", pos);
            }
        }
    }
}