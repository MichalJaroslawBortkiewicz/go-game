package com.mycompany.app.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.mycompany.app.board.GameManager;
import com.mycompany.app.board.exceptions.IncorrectStonePlacementException;
import com.mycompany.app.board.exceptions.NotYourTurnException;
import com.mycompany.app.database.IDataBaseManager;

public class TwoPlayerSession implements Session {
    private Socket firstPlayer;
    private Socket secondPlayer;
    private DataOutputStream[] playerStream;
    private DataInputStream[] playerIn;
    private IDataBaseManager dataBaseManager;
    private int size;

    private boolean gamesON;
    private boolean passed = false;
    private Receiver r1;
    private Receiver r2;

    private int x;
    private int y;
    private int player;

    private char[][] proposition;

    @Override
    public void run() {
        System.out.println("Session run");
        startGame();
        System.out.println("Game started");
        try {
            r1 = new Receiver(this, firstPlayer, 0);
            Thread thread = new Thread(r1);
            thread.start();
            r2 = new Receiver(this, secondPlayer, 1);
            thread = new Thread(r2);
            thread.start();
            playerStream[0] = new DataOutputStream(firstPlayer.getOutputStream());
            playerStream[1] = new DataOutputStream(secondPlayer.getOutputStream());
            playerIn[0] = new DataInputStream(firstPlayer.getInputStream());
            playerIn[1] = new DataInputStream(secondPlayer.getInputStream());
            playerStream[0].writeBoolean(true);
            playerStream[1].writeBoolean(false);
            System.out.println("Each player get if he is first");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        while (gamesON) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ex) {}
            System.out.println("Session notified");
            try {
                if (x == -2) {
                    int currentPlayer = GameManager.getInstance().isWhitePlays() ? 1 : 0;
                    if (currentPlayer == player) {
                        for (int i = 0; i < size; i++) {
                            for (int j = 0; j < size; j++) {
                                playerStream[1-currentPlayer].writeChar('\0');
                            }
                        }

                    } else {
                        dataBaseManager.saveMove("NULL");
                        playerStream[currentPlayer].writeBoolean(true);
                    }
                    
                    dataBaseManager.saveGame(size,-1, player == 1);
                    dataBaseManager.saveMove("FF");

                    break;
                }
                GameManager.getInstance().addStone(x, y, player);
                if (x == -1 && passed) {
                    System.out.print(playerStream[0].size());
                    System.out.print(playerStream[1].size());
                    dataBaseManager.saveMove("PASS");
                    playerStream[player].writeBoolean(false);
                    playerStream[player].writeBoolean(false);
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            playerStream[1-player].writeChar('\1');
                            playerStream[player].writeChar('\1');
                        }
                    }
                    r1.gamesOFF();
                    r2.gamesOFF();
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (InterruptedException ex) {}
                    if (x == -2) {
                        int currentPlayer = GameManager.getInstance().isWhitePlays() ? 1 : 0;
                        for (int i = 0; i < size; i++) {
                            for (int j = 0; j < size; j++) {
                                playerStream[1-currentPlayer].writeChar('\0');
                            }
                        }
                        break;
                    }
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            proposition[i][j] = playerIn[player].readChar();
                            System.out.print(proposition[i][j]);
                        }
                        System.out.println();
                    }
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            System.out.print(proposition[i][j]);
                            playerStream[1-player].writeChar(proposition[i][j]);
                        }
                    }
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (InterruptedException ex) {}
                    boolean accept = playerIn[player].readBoolean();
                    if (accept) {
                        int black = 0;
                        int white = 0;
                        for (int i = 0; i < size; i++) {
                            for (int j = 0; j < size; j++) {
                                if (proposition[i][j] == 'B') {
                                    black++;
                                } else if (proposition[i][j] == 'W') {
                                    white++;
                                }
                            }
                        }
                        GameManager.getInstance().addBlackPoints(black);
                        GameManager.getInstance().addWhitePoints(white);
                        System.out.println("accept");
                        playerStream[0].writeBoolean(false);
                        playerStream[1].writeBoolean(false);
                        int whiteHandicup = GameManager.getInstance().getWhitePoints() - GameManager.getInstance().getBlackPoints();
                        playerStream[0].writeInt(-whiteHandicup);
                        playerStream[1].writeInt(whiteHandicup);
                         
                        if(whiteHandicup >= 0){
                            dataBaseManager.saveGame(size, whiteHandicup, false);
                        }
                        else{
                            dataBaseManager.saveGame(size, -whiteHandicup, true);
                        }
                        break;

                    } else {
                        passed = false;
                        playerStream[1-player].writeBoolean(true);
                        GameManager.getInstance().nextPlayer();
                        r1 = new Receiver(this, firstPlayer, 0);
                        Thread thread = new Thread(r1);
                        thread.start();
                        r2 = new Receiver(this, secondPlayer, 1);
                        thread = new Thread(r2);
                        thread.start();
                    }
                    continue;
                }
                if (x == -1 && !passed) {
                    dataBaseManager.saveMove("PASS");
                    passed = true;
                    playerStream[player].writeBoolean(false);
                    playerStream[player].writeBoolean(false);
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            playerStream[1-player].writeChar('\n');
                            playerStream[player].writeChar('\n');
                        }
                    }
                    continue;
                }
                passed = false;
                System.out.println("Stone added");
                dataBaseManager.saveMove(x + ":" + y);
                playerStream[player].writeBoolean(false);
                playerStream[player].writeBoolean(false);
                char[][] board = GameManager.getInstance().getSimplifiedBoard();
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        playerStream[0].writeChar(board[i][j]);
                        playerStream[1].writeChar(board[i][j]);
                    }
                }
            } catch (IncorrectStonePlacementException | NotYourTurnException ex) {
                try {
                    playerStream[player].writeBoolean(false);
                    playerStream[player].writeBoolean(true);
                    playerStream[player].writeInt(ex.getMessage().getBytes().length);
                    playerStream[player].writeBytes(ex.getMessage());
                } catch (IOException ioex) {
                    System.err.println(ioex.getMessage());
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    @Override
    public void startGame() {
        GameManager.getInstance().startGame(size);
    }

    @Override
    public synchronized void addStone(int x, int y, int player) {
        System.out.println("Received move: " + x + " " + y + " from player " + player);
        this.x = x;
        this.y = y;
        this.player = player;
    }

    @Override
    public synchronized void setProposition(char[][] proposition) {
        this.proposition = proposition;
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    @Override
    public void endGame() {
        r1.gamesOFF();
        r2.gamesOFF();
        gamesON = false;
        try {
            firstPlayer.close();
            secondPlayer.close(); 
        } catch (IOException ex) {}
    }
    
    public TwoPlayerSession(Socket firstPlayer, Socket secondPlayer, int size, IDataBaseManager dataBaseManager)
    {
        if (Math.random() < 0.5) {
            this.firstPlayer = firstPlayer;
            this.secondPlayer = secondPlayer;
        } else {
            this.firstPlayer = secondPlayer;
            this.secondPlayer = firstPlayer;
        }
        this.dataBaseManager = dataBaseManager;
        playerStream = new DataOutputStream[2];
        playerIn = new DataInputStream[2];
        this.size = size;
        proposition = new char[size][size];
        gamesON = true;
        System.out.println("Two player session created");
    }
}
