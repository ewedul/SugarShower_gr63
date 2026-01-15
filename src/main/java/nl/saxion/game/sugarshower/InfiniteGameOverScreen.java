package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class InfiniteGameOverScreen extends ScalableGameScreen {
    float worldWidth;
    float worldHeight;

    private int selectedButton = 0;
    private static int waveReached = 1;
    private static int highestWave = 1;

    public InfiniteGameOverScreen() {
        super(800, 800);
    }

    public static void setWaveReached(int wave) {
        waveReached = wave;
        if (wave > highestWave) {
            highestWave = wave;
        }
    }


    @Override
    public void show() {
        worldWidth = GameApp.getWorldWidth();
        worldHeight = GameApp.getWorldHeight();

        GameApp.addTexture("gameover_background", "textures/gameover_bg.png");

        GameApp.addFont("bubble", "fonts/bubble.ttf", 20);
        GameApp.addFont("bubble_big", "fonts/bubble.ttf", 35);

        GameApp.addTexture("mainmenu", "textures/buttons/mainmenu_button.png");
        GameApp.addTexture("retry", "textures/buttons/retry_button.png");
        GameApp.addTexture("speech", "textures/speech_bubble.png");

        GameApp.addTexture("button","textures/buttons/button2.png");
        GameApp.addColor("customcolor", 64, 15, 38);
        GameApp.addMusic("gameover-music", "audio/gameover-music.mp3");
        AudioControl.playMusic("gameover-music", true, YourGameScreen.bgMusicVolume);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // Navigate with UP/DOWN arrow keys
        if (GameApp.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedButton = (selectedButton + 1) % 2;
        }
        else if (GameApp.isKeyJustPressed(Input.Keys.UP)) {
            selectedButton = (selectedButton - 1 + 2) % 2;
        }

        // Confirm selection with ENTER
        if (GameApp.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedButton == 0) {
                InfiniteGameScreen.resetGame(); // Use reset method instead
                GameApp.switchScreen("InfiniteGameScreen");
            } else {
                GameApp.switchScreen("MainMenuScreen");
            }
        }

        if (GameApp.isKeyJustPressed(Input.Keys.M)) {
            AudioControl.toggleMuteMode();
            GameApp.getMusic("gameover-music").
                    setVolume(AudioControl.muteMode ? 0f : YourGameScreen.bgMusicVolume);
        }



        float normalWidth = 254;
        float normalHeight = 100;
        float selectedWidth = 274;
        float selectedHeight = 120;

        float centerX = getWorldWidth() / 2;
        float startButtonY = (getWorldHeight()-300) / 2 + 46;
        float exitButtonY = (getWorldHeight()-300) / 2 - 46;

        float mouseX = getMouseX();
        float mouseY = getMouseY();

        float startX = centerX-normalWidth/2;
        float startY = startButtonY - normalHeight/2;
        float exitY = exitButtonY - normalHeight/2;

        if (GameApp.pointInRect(mouseX, mouseY, startX, startY, normalWidth, normalHeight)) {
            selectedButton = 0;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
                InfiniteGameScreen.resetGame(); // Use reset method instead
                GameApp.switchScreen("InfiniteGameScreen");
            }
        } else if (GameApp.pointInRect(mouseX, mouseY, startX, exitY, normalWidth, normalHeight)) {
            selectedButton = 1;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameApp.switchScreen("MainMenuScreen");
            }
        }

        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        GameApp.drawTexture("gameover_background", 0, 0, getWorldWidth(), getWorldHeight());

        if (selectedButton == 0) {
            GameApp.drawTextureCentered("retry",
                    centerX, startButtonY,
                    selectedWidth, selectedHeight);
            GameApp.drawTextureCentered("mainmenu",
                    centerX, exitButtonY,
                    normalWidth, normalHeight);

        } else if (selectedButton ==1){
            GameApp.drawTextureCentered("retry",
                    centerX, startButtonY,
                    normalWidth, normalHeight);
            GameApp.drawTextureCentered("mainmenu",
                    centerX, exitButtonY,
                    selectedWidth, selectedHeight);
        }

        GameApp.drawTextHorizontallyCentered("bubble_big","Game Over!", centerX, getWorldHeight()-50,"customcolor");

        float speechWidth = 331;
        float speechHeight = 205;

        float speechX = (centerX - speechWidth / 2) - 10;
        float speechY = startButtonY + 120;

        GameApp.drawTexture("speech", speechX, speechY, speechWidth, speechHeight);

        // Draw text inside speech bubble (centered)
        String message = "Looks like";
        String message2 = "things got a";
        String message3 = "little messy..";

        // Calculate text position to center it in the speech bubble
        float textCenterX = centerX;
        float textCenterY = speechY + (speechHeight / 2) + 20; // Adjust Y to center in bubble

        // Draw text centered in speech bubble
        GameApp.drawTextHorizontallyCentered("bubble", message,
                textCenterX, textCenterY + 25 , "customcolor");
        GameApp.drawTextHorizontallyCentered("bubble", message2,
                textCenterX, textCenterY, "customcolor"); // Second line below
        GameApp.drawTextHorizontallyCentered("bubble", message3,
                textCenterX, textCenterY - 25, "customcolor");

        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {
        GameApp.stopMusic("gameover-music");
        GameApp.disposeTexture("gameover_background");
        GameApp.disposeTexture("button");
        GameApp.disposeTexture("retry");
        GameApp.disposeTexture("mainmenu");
        GameApp.disposeTexture("speech");
        GameApp.disposeFont("bubble");
        GameApp.disposeFont("bubble_speech");
        GameApp.disposeMusic("gameover-music");
    }
}