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
        // Load background
        GameApp.addTexture("mainmenu", "textures/mainmenu.png");

        // Load button textures - NORMAL state
        GameApp.addTexture("start_button_normal", "textures/buttons/start_normal.png");
        GameApp.addTexture("exit_button_normal", "textures/buttons/exit_normal.png");

        // Load button textures - SELECTED state
        GameApp.addTexture("start_button_selected", "textures/buttons/start_selected.png");
        GameApp.addTexture("exit_button_selected", "textures/buttons/exit_selected.png");

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

        // ===== RENDERING =====
        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        // Draw background (full screen)
        GameApp.drawTexture("mainmenu", 0, 0, getWorldWidth(), getWorldHeight());

        // Button dimensions
        float normalWidth = 300;
        float normalHeight = 180;
        float selectedWidth = 350;
        float selectedHeight = 230;

        // Button positions (centered horizontally)
        float centerX = getWorldWidth() / 2;
        float startButtonY = (getWorldHeight()-300) / 2 + 67; // Higher position
        float exitButtonY = (getWorldHeight()-300) / 2 - 67;  // Lower position

        // Draw START button (centered)
        if (selectedButton == 0) {
            // Start button is SELECTED - use larger highlighted texture
            GameApp.drawTextureCentered("start_button_selected",
                    centerX, startButtonY,
                    selectedWidth, selectedHeight);
        } else {
            // Start button is NOT selected - use normal texture
            GameApp.drawTextureCentered("start_button_normal",
                    centerX, startButtonY,
                    normalWidth, normalHeight);
        }

        // Draw EXIT button (centered)
        if (selectedButton == 1) {
            // Exit button is SELECTED - use larger highlighted texture
            GameApp.drawTextureCentered("exit_button_selected",
                    centerX, exitButtonY,
                    selectedWidth, selectedHeight);
        } else {
            // Exit button is NOT selected - use normal texture
            GameApp.drawTextureCentered("exit_button_normal",
                    centerX, exitButtonY,
                    normalWidth, normalHeight);
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
    }
}