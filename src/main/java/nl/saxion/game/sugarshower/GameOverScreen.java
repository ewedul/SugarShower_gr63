package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.Mp3;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.gameobject.GameObject;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class GameOverScreen extends ScalableGameScreen {
    float worldWidth;
    float worldHeight;

    int selectedItem = 0;

    public GameOverScreen() {
        super(800, 800);
    }

    @Override
    public void show() {

        worldWidth = GameApp.getWorldWidth();
        worldHeight = GameApp.getWorldHeight();

        GameApp.addTexture("background", "textures/pink-bg.png");
        GameApp.addFont("GameOver", "fonts/basic.ttf", 30);

        GameApp.addMusic("GameOver-sound", "audio/funny_embarrassment_soundtrack.mp3");
        GameApp.playMusic("GameOver-sound", true, 0.2f);


    }

    @Override
    public void render(float delta) {
        super.render(delta);

        //Input logic
        if (GameApp.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedItem = (selectedItem + 1) % 2;
        } else if (GameApp.isKeyJustPressed(Input.Keys.UP)) {
            selectedItem -= 1;
            if (selectedItem < 0) {
                selectedItem = 1;
            }
        }
        if (GameApp.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedItem == 0) {
                GameApp.switchScreen("MainMenuScreen");
            } else {
                GameApp.quit();
            }
        }


        //Rendering
        GameApp.clearScreen();

        GameApp.startSpriteRendering();
        GameApp.drawTexture("background", 0, 0, worldWidth, worldHeight);
        GameApp.drawTextHorizontallyCentered("GameOver", "Looks like things got a little messy...", worldWidth / 2, worldHeight / 2 + 50, "teal-900");

        if (selectedItem == 0) {
            GameApp.drawTextHorizontallyCentered("GameOver", "Retry the level", worldWidth / 2, worldHeight / 2, "yellow-500");
        } else {
            GameApp.drawTextHorizontallyCentered("GameOver", "Retry the level", worldWidth / 2, worldHeight / 2, "teal-900");
        }
        if (selectedItem == 1) {
            GameApp.drawTextHorizontallyCentered("GameOver", "Quit", worldWidth / 2, worldHeight / 2 - 50, "yellow-500");
        } else {
            GameApp.drawTextHorizontallyCentered("GameOver", "Quit", worldWidth / 2, worldHeight / 2 - 50, "teal-900");
        }

        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {

        GameApp.stopMusic("GameOver-sound");
        GameApp.disposeTexture("background");
        GameApp.disposeFont("GameOver");
        GameApp.disposeMusic("GameOver-sound");
    }
}
