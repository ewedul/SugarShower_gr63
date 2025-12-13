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

        GameApp.addMusic("gameover-music", "audio/gameover-music.mp3");
        AudioControl.playMusic("gameover-music", true, YourGameScreen.bgMusicVolume);

    }

    @Override
    public void render(float delta) {
        super.render(delta);

        //=================Input logic=================
        if (GameApp.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedItem = (selectedItem + 1) % 2;
        } else if (GameApp.isKeyJustPressed(Input.Keys.UP)) {
            selectedItem -= 1;
            if (selectedItem < 0) {
                selectedItem = 1;
            }
        }
        // retry the level
        if (GameApp.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedItem == 0) {

                YourGameScreen.setLevel(YourGameScreen.getCurrentLevel());
                GameApp.switchScreen("YourGameScreen");


            } else {
                GameApp.quit();
            }
        }

        //Press M to turn on Mute Mode.
        if (GameApp.isKeyJustPressed(Input.Keys.M)) {
            AudioControl.toggleMuteMode();
            GameApp.getMusic("gameover-music").
                    setVolume(AudioControl.muteMode ? 0f : YourGameScreen.bgMusicVolume);
        }

        //=================Graphic rendering=================//
        GameApp.clearScreen();

        GameApp.startSpriteRendering();
        GameApp.drawTexture("background", 0, 0, worldWidth, worldHeight);
        GameApp.drawTextHorizontallyCentered("GameOver", "Looks like things got a little messy...", worldWidth / 2, worldHeight / 2 + 50, "teal-900");

        if (selectedItem == 0) {
            GameApp.drawTextHorizontallyCentered("GameOver", "Retry the level",
                    worldWidth / 2, worldHeight / 2, "yellow-500");

            GameApp.drawTextHorizontallyCentered("GameOver", "Quit",
                    worldWidth / 2, worldHeight / 2 - 50, "teal-900");

        } else if (selectedItem == 1) {
            GameApp.drawTextHorizontallyCentered("GameOver", "Retry the level",
                    worldWidth / 2, worldHeight / 2, "teal-900");

            GameApp.drawTextHorizontallyCentered("GameOver", "Quit",
                    worldWidth / 2, worldHeight / 2 - 50, "yellow-500");

        }

        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {

        GameApp.stopMusic("gameover-music");
        GameApp.disposeTexture("background");
        GameApp.disposeFont("GameOver");
        GameApp.disposeMusic("gameover-music");
    }
}
