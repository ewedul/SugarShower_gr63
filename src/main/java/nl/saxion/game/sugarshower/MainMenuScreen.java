package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class MainMenuScreen extends ScalableGameScreen {
    public MainMenuScreen() {
        super(800, 800);
    }

    @Override
    public void show() {
        GameApp.addTexture("mainmenu", "textures/mainmenu.png");
        GameApp.addFont("basic", "fonts/basic.ttf", 50);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // When the user presses enter, go to the next screen
        if (GameApp.isKeyJustPressed(Input.Keys.ENTER)) {
            GameApp.switchScreen("YourGameScreen");
        }

        // Render the main menu
        GameApp.clearScreen();
        GameApp.startSpriteRendering();
        GameApp.drawTexture("mainmenu", 0, 0, getWorldWidth(), getWorldHeight());
        GameApp.drawTextCentered("basic", "Press ENTER to Start", getWorldWidth()/2, 150, "amber-500");
        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {
        GameApp.disposeFont("basic");
        GameApp.disposeTexture("mainmenu");
    }
}