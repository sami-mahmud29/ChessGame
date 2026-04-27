package com.example.chessgame.model;

public abstract class Piece {
    protected String color;
    protected Position position;
    protected boolean hasMoved = false;

    public Piece(String color, Position position) {
        this.color = color;
        this.position = position;
    }

    public String getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }
}