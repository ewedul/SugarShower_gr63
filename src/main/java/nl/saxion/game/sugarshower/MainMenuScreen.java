package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class MainMenuScreen extends ScalableGameScreen {

    // Track which button is selected (0 = Start, 1 = Exit)
    private int selectedButton = 0;

    public MainMenuScreen() {
        super(800, 800);
    }

    @Override
    public void show() {

        GameApp.setCustomCursor("textures/strawberry_64.png",0,0);
        // Load background
        GameApp.addTexture("mainmenu", "textures/mainmenu.png");

        // Load button textures - NORMAL state
        GameApp.addTexture("start_button_normal", "textures/buttons/start_normal.png");
        GameApp.addTexture("exit_button_normal", "textures/buttons/exit_normal.png");

        // Load button textures - SELECTED state
        GameApp.addTexture("start_button_selected", "textures/buttons/start_selected.png");
        GameApp.addTexture("exit_button_selected", "textures/buttons/exit_selected.png");

        GameApp.addTexture("unmute-button", "textures/buttons/unmute-pixel.png");
        GameApp.addTexture("mute-button", "textures/buttons/mute-pixel.png");

        // Load font for additional text
        GameApp.addFont("basic", "fonts/basic.ttf", 50);
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
                // Start button pressed - go to game
                GameApp.switchScreen("YourGameScreen");
            } else {
                // Exit button pressed - quit game
                GameApp.quit();
            }
        }

        // Button dimensions
        float normalWidth = 300;
        float normalHeight = 180;
        float selectedWidth = 350;
        float selectedHeight = 230;

        // Button positions (centered horizontally)
        float centerX = getWorldWidth() / 2;
        float startButtonY = (getWorldHeight()-300) / 2 + 67; // Higher position
        float exitButtonY = (getWorldHeight()-300) / 2 - 67;  // Lower position

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
                GameApp.switchScreen("YourGameScreen");
            }
        } else if (GameApp.pointInRect(mouseX, mouseY, startX, exitY, normalWidth, normalHeight)) {
            selectedButton = 1;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameApp.quit();
            }
        }

        float muteButtonSize = 50f;
        float muteX = getWorldWidth() - (float) 1.2 * muteButtonSize;
        float muteY = getWorldHeight() - (float) 1.2 * muteButtonSize;

        Button muteButton = new Button(muteX, muteY, muteButtonSize, muteButtonSize);

        // Press M or click mute icon to mute audios
        if (GameApp.isKeyJustPressed(Input.Keys.M)
                || muteButton.isButtonClicked(getMouseX(), getMouseY())) {
            AudioControl.toggleMuteMode();
        }

        // ===== RENDERING =====
        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        // Draw background (full screen)
        GameApp.drawTexture("mainmenu", 0, 0, getWorldWidth(), getWorldHeight());

        // Draw button (centered)
        if (selectedButton == 0) {
            // Start button is SELECTED - use larger highlighted texture
            GameApp.drawTextureCentered("start_button_selected",
                    centerX, startButtonY,
                    selectedWidth, selectedHeight);
            // Exit button is NOT selected - use normal texture
            GameApp.drawTextureCentered("exit_button_normal",
                    centerX, exitButtonY,
                    normalWidth, normalHeight);

        } else if (selectedButton == 1) {
            // Start button is NOT selected - use normal texture
            GameApp.drawTextureCentered("start_button_normal",
                    centerX, startButtonY,
                    normalWidth, normalHeight);
            // Exit button is SELECTED - use larger highlighted texture
            GameApp.drawTextureCentered("exit_button_selected",
                    centerX, exitButtonY,
                    selectedWidth, selectedHeight);
        }

        //Draw mute button
        if (AudioControl.muteMode) {
            GameApp.drawTexture("unmute-button", muteX, muteY, muteButtonSize, muteButtonSize);
        } else {
            GameApp.drawTexture("mute-button", muteX, muteY, muteButtonSize, muteButtonSize);
        }

        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {
        // Clean up all textures and fonts
        GameApp.disposeFont("basic");
        GameApp.disposeTexture("mainmenu");
        GameApp.disposeTexture("start_button_normal");
        GameApp.disposeTexture("exit_button_normal");
        GameApp.disposeTexture("start_button_selected");
        GameApp.disposeTexture("exit_button_selected");
        GameApp.disposeTexture("unmute-button");
        GameApp.disposeTexture("mute-button");
    }
}