package com.mycompany.app.board;

import com.mycompany.app.board.exceptions.IncorrectStonePlacementException;
import com.mycompany.app.board.exceptions.NotYourTurnException;

public class GameManager {
    private static ThreadLocal<GameManager> instance = new ThreadLocal<GameManager>() {
        @Override
        protected GameManager initialValue() {
            return new GameManager();
        }
    };

    private Board board;
    private int whitePoints = 0;
    private int blackPoints = 0;
    private boolean whitePlays = false;

    public Board getBoard() {
        return board;
    }

    public void addWhitePoints(int points) {
        whitePoints += points;
    }

    public int getWhitePoints() {
        return whitePoints;
    }

    public void addBlackPoints(int points) {
        blackPoints += points;
    }

    public int getBlackPoints() {
        return blackPoints;
    }

    public boolean isWhitePlays() {
        return whitePlays;
    }

    public void startGame(int size) {
        board = new Board(size);
        whitePoints = 6;
        blackPoints = 0;
        whitePlays = false;
    }

    public void addStone(int x, int y, int player) throws IncorrectStonePlacementException, NotYourTurnException {
        if ((player == 1) != whitePlays) {
            throw new NotYourTurnException();
        }
        if (x != -1) {
            board.addStone(x, y, player == 1 ? StoneColor.WHITE : StoneColor.BLACK);
            System.out.println("board.addStone");
        }
        nextPlayer();
    }

    public boolean addStoneAsBot(StoneColor botColor) {
        nextPlayer();
        for (int i = 0; i < 10; i++) {
            try {
                board.addStone((int)(Math.random() * board.getSize()), (int)(Math.random() * board.getSize()), botColor);
                return true;
            } catch (IncorrectStonePlacementException ex) {
                System.out.println("Trying again");
            }
        }
        return false;
    }

    public void nextPlayer() {
        whitePlays = !whitePlays;
    }

    public char[][] getSimplifiedBoard() {
        return board.getSimplifiedBoard();
    }

    public static GameManager getInstance() {
        return instance.get();
    }

    public static void resetInstance() {
        instance.remove();
    }
}
