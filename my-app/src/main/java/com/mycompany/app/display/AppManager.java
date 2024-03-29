package com.mycompany.app.display;

import java.io.IOException;

import com.mycompany.app.client.Client;
import com.mycompany.app.client.exceptions.FromServerException;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class AppManager {
    private static AppManager instance;
    
    private App app;
    private boolean myTurn;
    private Client client;
    private GameScene gameScene;
    private boolean gameCanceled;
    private boolean surrender = false;

    public void startGame(int size) {
        System.out.println("Waiting for player to join...");
        client.confirmGame();
        gameScene = app.startGame(size);
        if (myTurn) {
            waitForOpponentsSurrender();
        }
        else {
            waitForOpponentsMove();
        }
    }

    public void startDataBase(int size){
        app.startDataBase(size);
    }

    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public char[][] sendMove(int i, int j)
    {
        if (!myTurn) {
            return null;
        }
        char[][] boardState = null;
        try {
            System.out.println("Asking client to send move");
            boardState = client.sendMove(i, j);
            for (int i1 = 0; i1 < 9; i1++)
            {
                for (int j1 = 0; j1 < 9; j1++)
                {
                    System.out.print(boardState[i1][j1]);
                }
            }
            System.out.println();
            System.out.println(boardState);
            myTurn = false;
        } catch (IOException ex) {
            System.err.println("Connection with server failed: " + ex.getMessage());
        } catch (FromServerException ex) {
            System.err.println("Server returned exception: " + ex.getMessage());
            waitForOpponentsSurrender();
        }
        return boardState;
    }

    public void enterProposingMode() {
        gameScene.enterProposingMode();
    }

    public void exitPropositionMode() {
        gameScene.exitPropositionMode();
    }

    public void sendProposition(char[][] proposition) {
        client.sendProposition(proposition);
    }

    public void enterJudgeMode() {
        gameScene.enterJudgeMode();
    }

    public int sendDecision(boolean decision) {
        return client.sendDecision(decision);
    }

    public char[][] sendMoveNr(int moveNr) throws IOException, FromServerException{
        return client.sendMoveNr(moveNr);
    }

    public int nextGame(){
        try{
            return client.nextGame();
        }
        catch(IOException ex){
            return -1;
        }
    }

    public int prevGame(){
        try{
            return client.prevGame();
        }
        catch(IOException ex){
            return -1;
        }
    }

    public void surrender() {
        try {
            surrender = true;
            if (client != null) {
                client.surrender();
            }
        } catch (IOException ex) {
            System.err.println("Connection with server failed: " + ex.getMessage());
        }
        endGame();
    }

    public void waitForOpponentsMove() {
        client.waitForOpponentsMove();
    }

    public void waitForGameStart(Dialog<ButtonType> dialog) {
        client.waitForGameStart(dialog);
    }

    public void waitForOpponentsSurrender() {
        client.waitForOpponentsSurrender();
    }

    public void waitForDecision() {
        client.waitForDecision();
    }

    public void cancelGame() {
        client.cancelGame();
    }

    public void setApp(App app) {
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    public void setClient(Client client) {
        instance.client = client;
    }

    public Client getClient() {
        return instance.client;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public boolean isGameCanceled() {
        return gameCanceled;
    }

    public void setGameCanceled(boolean gameCanceled) {
        this.gameCanceled = gameCanceled;
    }

    public GameScene getGameScene() {
        return gameScene;
    }

    public boolean isSurrender() {
        return surrender;
    }

    public void endGame() {
        if (client != null) {
            client.closeSocket();
        }
        app.endGame();
    }

    public synchronized static AppManager getInstance() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }
}
