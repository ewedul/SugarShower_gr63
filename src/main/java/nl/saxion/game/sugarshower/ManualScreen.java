package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.Mp3;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.gameobject.GameObject;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class ManualScreen extends ScalableGameScreen {
    float worldWidth;
    float worldHeight;

    public ManualScreen() {
        super(800, 800);
    }

    @Override
    public void show() {
        worldWidth = GameApp.getWorldWidth();
        worldHeight = GameApp.getWorldHeight();

        GameApp.addTexture("cross", "textures/cross.png");
        GameApp.addTexture("manual", "textures/manual.png");

    }

    @Override
    public void render(float delta) {
        super.render(delta);

        float crossButtonSize = 50f;
        float crossX = getWorldWidth() - (float) 1.2 * crossButtonSize;
        float crossY = getWorldHeight() - (float) 1.2 * crossButtonSize;

        // Button positions (centered horizontally)
        float centerX = getWorldWidth() / 2;

        //Use mouse input to choose
        float mouseX = getMouseX();
        float mouseY = getMouseY();



        //Button changes size when hovering mouse over it. Click to choose.

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
        GameApp.disposeFont("cross");
        GameApp.disposeFont("manual");

    }
}