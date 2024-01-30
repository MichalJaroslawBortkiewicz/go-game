package com.mycompany.app.display;

import java.io.IOException;

import com.mycompany.app.client.Client;
import com.mycompany.app.client.exceptions.FromServerException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MenuScene extends Group {

    public MenuScene()
    {
        final Button gameButton9noBot = new Button("Player  9x9 ");
        final Button gameButton13noBot = new Button("Player 13x13");
        final Button gameButton19noBot = new Button("Player 19x19");
        final Button gameButton9withBot = new Button("Bot  9x9");
        final Button gameButton13withBot = new Button("Bot 13x13");
        final Button gameButton19withBot = new Button("Bot 19x19");

        gameButton9noBot.setOnAction(new GameButtonHandler(9, false));
        gameButton13noBot.setOnAction(new GameButtonHandler(13, false));
        gameButton19noBot.setOnAction(new GameButtonHandler(19, false));
        gameButton9withBot.setOnAction(new GameButtonHandler(9, true));
        gameButton13withBot.setOnAction(new GameButtonHandler(13, true));
        gameButton19withBot.setOnAction(new GameButtonHandler(19, true));

        final GridPane inputGridPane = new GridPane();
        
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);

        inputGridPane.add(gameButton9noBot, 0, 0);
        inputGridPane.add(gameButton13noBot, 0, 1);
        inputGridPane.add(gameButton19noBot, 0, 2);
        inputGridPane.add(gameButton9withBot, 1, 0);
        inputGridPane.add(gameButton13withBot, 1, 1);
        inputGridPane.add(gameButton19withBot, 1, 2);

        getChildren().add(inputGridPane);
    }

    final class GameButtonHandler implements EventHandler<ActionEvent> {
        private final int boardSize;
        private final boolean withBot;

        public GameButtonHandler(int boardSize, boolean withBot) {
            this.boardSize = boardSize;
            this.withBot = withBot;
        }

        @Override
        public void handle(ActionEvent event) {
            try {
                AppManager.getInstance().setClient(new Client(boardSize, withBot));
                AppManager.getInstance().startGame(boardSize);
            } catch (IOException ex) {
                System.err.println("Connection with server failed: " + ex.getMessage());
            } catch (FromServerException ex) {
                System.err.println("Server send exception: " + ex.getMessage());
            }
        }
    }
}
