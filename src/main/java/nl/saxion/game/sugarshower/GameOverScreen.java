package nl.saxion.game.sugarshower;
import com.badlogic.gdx.Input;

import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class GameOverScreen extends ScalableGameScreen {
    float worldWidth;
    float worldHeight;

    private int selectedButton = 0;

    public GameOverScreen() {
        super(800, 800);
    }

    @Override
    public void show() {
        worldWidth = GameApp.getWorldWidth();
        worldHeight = GameApp.getWorldHeight();

        GameApp.addTexture("gameover_background", "textures/gameover_bg.png");

        // Add fonts for speech bubble text
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

        // ===== INPUT HANDLING =====
        // Navigate with UP/DOWN arrow keys
        if (GameApp.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedButton = (selectedButton + 1) % 2; // Cycle: 0 -> 1 -> 0
        }
        else if (GameApp.isKeyJustPressed(Input.Keys.UP)) {
            selectedButton = (selectedButton - 1 + 2) % 2; // Cycle: 1 -> 0 -> 1
        }

        // Confirm selection with ENTER
        if (GameApp.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedButton == 0) {
                // Retry button pressed - go back to game
//                YourGameScreen.setLevel(YourGameScreen.getCurrentLevel());
                GameApp.switchScreen("YourGameScreen");
            } else {
                // Main menu button pressed
                GameApp.switchScreen("MainMenuScreen");
            }
        }

        //Press M to turn on Mute Mode.
        if (GameApp.isKeyJustPressed(Input.Keys.M)) {
            AudioControl.toggleMuteMode();
            GameApp.getMusic("gameover-music").
                    setVolume(AudioControl.muteMode ? 0f : YourGameScreen.bgMusicVolume);
        }

        // Button dimensions
        float normalWidth = 254;
        float normalHeight = 100;
        float selectedWidth = 274;
        float selectedHeight = 120;

        // Button positions (centered horizontally)
        float centerX = getWorldWidth() / 2;
        float startButtonY = (getWorldHeight()-300) / 2 + 46; // Higher position
        float exitButtonY = (getWorldHeight()-300) / 2 - 46;  // Lower position

        //Use mouse input to choose
        float mouseX = getMouseX();
        float mouseY = getMouseY();

        //World coordinate of button (different due to drawTextureCentered method)
        float startX = centerX-normalWidth/2;
        float startY = startButtonY - normalHeight/2;
        float exitY = exitButtonY - normalHeight/2;

        //Button changes size when hovering mouse over it. Click to choose.
        if (GameApp.pointInRect(mouseX, mouseY, startX, startY, normalWidth, normalHeight)) {
            selectedButton = 0;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
//                YourGameScreen.setLevel(YourGameScreen.getCurrentLevel());
                GameApp.switchScreen("YourGameScreen");
            }
        } else if (GameApp.pointInRect(mouseX, mouseY, startX, exitY, normalWidth, normalHeight)) {
            selectedButton = 1;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameApp.switchScreen("MainMenuScreen");
            }
        }


        // ===== RENDERING =====
        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        // Draw background (full screen)
        GameApp.drawTexture("gameover_background", 0, 0, getWorldWidth(), getWorldHeight());

        // Draw button (centered)
        if (selectedButton == 0) {
            // retry button is SELECTED - use larger highlighted texture
            GameApp.drawTextureCentered("retry",
                    centerX, startButtonY,
                    selectedWidth, selectedHeight);
            // mainmenu button is NOT selected - use normal texture
            GameApp.drawTextureCentered("mainmenu",
                    centerX, exitButtonY,
                    normalWidth, normalHeight);

        } else if (selectedButton ==1){
            // retry button is NOT selected - use normal texture
            GameApp.drawTextureCentered("retry",
                    centerX, startButtonY,
                    normalWidth, normalHeight);
            // mainmenu button is SELECTED - use larger highlighted texture
            GameApp.drawTextureCentered("mainmenu",
                    centerX, exitButtonY,
                    selectedWidth, selectedHeight);
        }

        // gameover
        GameApp.drawTextHorizontallyCentered("bubble_big","Game Over!", centerX, getWorldHeight()-50,"customcolor");

        // ===== SPEECH BUBBLE =====
        // Speech bubble dimensions
        float speechWidth = 331;
        float speechHeight = 205;

        // Position speech bubble centered above the buttons
        float speechX = (centerX - speechWidth / 2) - 10;
        float speechY = startButtonY + 120; // Position above retry button

        // Draw speech bubble
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