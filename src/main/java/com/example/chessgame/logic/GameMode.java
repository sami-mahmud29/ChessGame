package com.example.chessgame.logic;

import com.example.chessgame.model.*;

public interface GameMode {

    boolean handleMove(Board board, Position from, Position to, String currentTurn);

    String getNextTurn(String currentTurn);

    void afterMove(Board board, String currentTurn);
}