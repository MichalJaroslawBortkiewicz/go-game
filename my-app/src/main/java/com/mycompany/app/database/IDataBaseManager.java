package com.mycompany.app.database;

import java.util.List;

public interface IDataBaseManager{
    void connect();
    void saveGame(int pointDifference, boolean blackWon);
    void saveMove(String move);
    List<String> readMoves(int moveNr);
}
