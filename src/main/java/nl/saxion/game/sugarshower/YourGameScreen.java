package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

public class YourGameScreen extends ScalableGameScreen {

    public static final int BOWL_SIZE = 150;
    public static final int BOWL_SPEED = 500;
    Bowl bowl;

    public YourGameScreen() {
        super(800, 800);
    }

//trytry
    //hewwo
    //Nhi

    @Override
    public void show() {
        bowl = new Bowl();
        bowl.x = 0;
        bowl.y = 0;

        GameApp.addTexture("background", "textures/candy-bg.png");
    }

    @Override
    public void render(float delta) {

        super.render(delta);
        GameApp.addTexture("bowl", "textures/bowl_01.png");



        handlePlayerInput(delta);

        if (GameApp.isKeyJustPressed(Input.Keys.ESCAPE)){
            GameApp.switchScreen("MainMenuScreen");

        }





        // Draw elements
        GameApp.clearScreen();
        GameApp.startSpriteRendering();
        GameApp.drawTexture("background", 0, 0, getWorldWidth(), getWorldHeight());

        GameApp.drawTexture("bowl", bowl.x, bowl.y, BOWL_SIZE, BOWL_SIZE);
        GameApp.endSpriteRendering();


    }


    public void handlePlayerInput(float delta) {
//        if (GameApp.isKeyPressed(Input.Keys.UP)) {
//            bowl.y = bowl.y + BOWL_SPEED * delta;
//
//        } else if (GameApp.isKeyPressed(Input.Keys.DOWN)) {
//            bowl.y = bowl.y - BOWL_SPEED * delta;
//        }
        if (GameApp.isKeyPressed(Input.Keys.LEFT)) {
            bowl.x = bowl.x - BOWL_SPEED * delta;

        } else if (GameApp.isKeyPressed(Input.Keys.RIGHT)) {
            bowl.x = bowl.x + BOWL_SPEED * delta;
        }

        bowl.y = GameApp.clamp(bowl.y, 0, getWorldHeight() - BOWL_SIZE);
        bowl.x = GameApp.clamp(bowl.x, 0, getWorldWidth() - BOWL_SIZE);
    }

    @Override
    public void hide() {
        GameApp.disposeTexture("bowl");
        GameApp.disposeTexture("background");

    }
}
