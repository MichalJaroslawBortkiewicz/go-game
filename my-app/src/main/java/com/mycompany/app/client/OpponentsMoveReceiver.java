package com.mycompany.app.client;

import java.io.DataInputStream;
import java.io.IOException;

import com.mycompany.app.display.AppManager;

import javafx.application.Platform;

public class OpponentsMoveReceiver implements Runnable {
    DataInputStream input;
    int size;

    @Override
    public void run() {
        char[][] board = new char[size][size];

        try {
            for(int i = 0; i < size; i++) {
                for(int j = 0; j < size; j++) {
                    board[i][j] = input.readChar();
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        if (board[0][0] == '\n') {
            System.out.println("Opponent passed their turn");
            AppManager.getInstance().setMyTurn(true);
            AppManager.getInstance().waitForOpponentsSurrender();
            return;
        }
        if (board[0][0] == '\0') {
            if (!AppManager.getInstance().isSurrender()) {
                System.out.println("Opponent resigned");
            }
            Platform.runLater( () -> AppManager.getInstance().endGame());
            return;
        }
        if (board[0][0] == '\1') {
            Platform.runLater( () -> AppManager.getInstance().enterProposingMode());
            return;
        }

        AppManager.getInstance().getGameScene().rearrange(board);
        AppManager.getInstance().setMyTurn(true);
        AppManager.getInstance().waitForOpponentsSurrender();
    }
    
    public OpponentsMoveReceiver(DataInputStream input, int size) {
        this.input = input;
        this.size = size;
    }
}
