package nl.saxion.game;

import nl.saxion.game.sugarshower.*;
import nl.saxion.gameapp.GameApp;

public class Main {
    public static void main(String[] args) {
        // Add screens
        GameApp.addScreen("MainMenuScreen", new MainMenuScreen());
        GameApp.addScreen("YourGameScreen", new YourGameScreen());
        GameApp.addScreen("GameOverScreen", new GameOverScreen());
        GameApp.addScreen("LevelCompleteScreen", new LevelCompleteScreen());
        GameApp.addScreen("ManualScreen", new ManualScreen());
        GameApp.addScreen("VictoryScreen", new VictoryScreen());

        // Start game loop and show main menu screen
        GameApp.start("Sugar Shower", 800, 800, 60, false, "MainMenuScreen");
    }
}
