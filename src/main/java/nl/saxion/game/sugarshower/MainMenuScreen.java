package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class MainMenuScreen extends ScalableGameScreen {

    // Track which button is selected (0 = Start, 1 = Infinite, 2 = Exit)
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
        GameApp.addTexture("infinite_button_normal", "textures/buttons/infinite_normal.png");
        GameApp.addTexture("exit_button_normal", "textures/buttons/exit_normal.png");
        GameApp.addTexture("help", "textures/questionmark.png");
        GameApp.addTexture("manual", "textures/manual.png");

        // Load button textures - SELECTED state
        GameApp.addTexture("start_button_selected", "textures/buttons/start_selected.png");
        GameApp.addTexture("infinite_button_selected", "textures/buttons/infinite_selected.png");
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
            selectedButton = (selectedButton + 1) % 3; // Cycle: 0 -> 1 -> 2 -> 0
        }
        else if (GameApp.isKeyJustPressed(Input.Keys.UP)) {
            selectedButton = (selectedButton - 1 + 3) % 3; // Cycle: 2 -> 1 -> 0 -> 2
        }

        // Confirm selection with ENTER
        if (GameApp.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedButton == 0) {
                // Start button pressed - go to game
                GameApp.switchScreen("YourGameScreen");
            } else if (selectedButton == 1) {
                // Infinite button pressed - go to infinite game
                GameApp.switchScreen("InfiniteGameScreen");
            } else {
                // Exit button pressed - quit game
                GameApp.quit();
            }
        }

        // Button dimensions (all slightly smaller)
        float normalWidth = 260-15;
        float normalHeight = 160-15;
        float selectedWidth = 310-15;
        float selectedHeight = 210-15;

        // Button positions (centered horizontally, closer spacing)
        float centerX = getWorldWidth() / 2;
        float startButtonY = (getWorldHeight()-300) / 2 + 170; // Top position
        float infiniteButtonY = (getWorldHeight()-300) / 2 + 70; // Middle position
        float exitButtonY = (getWorldHeight()-300) / 2 - 30;  // Bottom position

        //Use mouse input to choose
        float mouseX = getMouseX();
        float mouseY = getMouseY();

        //World coordinate of buttons (different due to drawTextureCentered method)
        float startX = centerX-normalWidth/2;
        float startY = startButtonY - normalHeight/2;
        float infiniteX = centerX-normalWidth/2;
        float infiniteY = infiniteButtonY - normalHeight/2;
        float exitX = centerX-normalWidth/2;
        float exitY = exitButtonY - normalHeight/2;

        //Button changes size when hovering mouse over it. Click to choose.
        if (GameApp.pointInRect(mouseX, mouseY, startX, startY, normalWidth, normalHeight)) {
            selectedButton = 0;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameApp.switchScreen("YourGameScreen");
            }
        } else if (GameApp.pointInRect(mouseX, mouseY, infiniteX, infiniteY, normalWidth, normalHeight)) {
            selectedButton = 1;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameApp.switchScreen("InfiniteGameScreen");
            }
        } else if (GameApp.pointInRect(mouseX, mouseY, exitX, exitY, normalWidth, normalHeight)) {
            selectedButton = 2;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameApp.quit();
            }
        }

        float muteButtonSize = 50f;
        float muteX = getWorldWidth() - (float) 1.2 * muteButtonSize;
        float muteY = getWorldHeight() - (float) 1.2 * muteButtonSize;

        Button muteButton = new Button(muteX, muteY, muteButtonSize, muteButtonSize);
        Button helpButton = new Button(0, getWorldHeight() - 60, muteButtonSize, muteButtonSize);

        // Press M or click mute icon to mute audios
        if (GameApp.isKeyJustPressed(Input.Keys.M)
                || muteButton.isButtonClicked(getMouseX(), getMouseY())) {
            AudioControl.toggleMuteMode();
        }

        if (GameApp.isKeyJustPressed(Input.Keys.H)
                || helpButton.isButtonClicked(getMouseX(), getMouseY())) {
            GameApp.switchScreen("ManualScreen");
        }

        // ===== RENDERING =====
        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        // Draw background (full screen)
        GameApp.drawTexture("mainmenu", 0, 0, getWorldWidth(), getWorldHeight());

        // Draw buttons (centered)
        if (selectedButton == 0) {
            // Start button is SELECTED
            GameApp.drawTextureCentered("start_button_selected",
                    centerX, startButtonY,
                    selectedWidth, selectedHeight);
            GameApp.drawTextureCentered("infinite_button_normal",
                    centerX, infiniteButtonY,
                    normalWidth, normalHeight);
            GameApp.drawTextureCentered("exit_button_normal",
                    centerX, exitButtonY,
                    normalWidth, normalHeight);

        } else if (selectedButton == 1) {
            // Infinite button is SELECTED
            GameApp.drawTextureCentered("start_button_normal",
                    centerX, startButtonY,
                    normalWidth, normalHeight);
            GameApp.drawTextureCentered("infinite_button_selected",
                    centerX, infiniteButtonY,
                    selectedWidth, selectedHeight);
            GameApp.drawTextureCentered("exit_button_normal",
                    centerX, exitButtonY,
                    normalWidth, normalHeight);

        } else if (selectedButton == 2) {
            // Exit button is SELECTED
            GameApp.drawTextureCentered("start_button_normal",
                    centerX, startButtonY,
                    normalWidth, normalHeight);
            GameApp.drawTextureCentered("infinite_button_normal",
                    centerX, infiniteButtonY,
                    normalWidth, normalHeight);
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

        // draw help button
        GameApp.drawTexture("help", 0, getWorldHeight() - 60, muteButtonSize, muteButtonSize);


        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {
        // Clean up all textures and fonts
        GameApp.disposeFont("basic");
        GameApp.disposeTexture("mainmenu");
        GameApp.disposeTexture("start_button_normal");
        GameApp.disposeTexture("infinite_button_normal");
        GameApp.disposeTexture("exit_button_normal");
        GameApp.disposeTexture("start_button_selected");
        GameApp.disposeTexture("infinite_button_selected");
        GameApp.disposeTexture("exit_button_selected");
        GameApp.disposeTexture("unmute-button");
        GameApp.disposeTexture("help");
        GameApp.disposeTexture("manual");
        GameApp.disposeTexture("mute-button");
    }
}