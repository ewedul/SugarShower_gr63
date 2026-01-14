package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class ManualScreen extends ScalableGameScreen {

    public ManualScreen() {
        super(800, 800);
    }

    @Override
    public void show() {

        GameApp.addTexture("cross", "textures/cross.png");
        GameApp.addTexture("manual", "textures/manual_02.png");

    }

    @Override
    public void render(float delta) {
        super.render(delta);

        float crossButtonSize = 50f;
        float crossX = getWorldWidth() - (float) 1.2 * crossButtonSize;
        float crossY = getWorldHeight() - (float) 1.2 * crossButtonSize;

        //Press key X or click on X button to exit.

        Button crossButton = new Button(crossX, crossY, crossButtonSize, crossButtonSize);

        if (GameApp.isKeyJustPressed(Input.Keys.X)
                || crossButton.isButtonClicked(getMouseX(), getMouseY())) {
            GameApp.switchScreen("MainMenuScreen");
        }

        // ===== RENDERING =====
        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        // Draw background (full screen)
        GameApp.drawTexture("manual", 0, 0, getWorldWidth(), getWorldHeight());
        GameApp.drawTexture("cross", crossX, crossY, crossButtonSize, crossButtonSize);

        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {
        GameApp.disposeTexture("cross");
        GameApp.disposeTexture("manual");

    }
}