package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;
import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.util.ArrayList;

import static com.badlogic.gdx.graphics.Color.WHITE;

public class YourGameScreen extends ScalableGameScreen {

    public static final int BOWL_SIZE = 150;
    public static final int BOWL_SPEED = 500;
    Bowl bowl;
    ArrayList<Ingredient> ingredientsList = new ArrayList<>();

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

        //read the ingredients from csv file
        ingredientsList = readCSV("src/main/resources/ingredients.csv");






        // Draw elements
        GameApp.clearScreen();
        GameApp.startSpriteRendering();
        GameApp.drawTexture("background", 0, 0, getWorldWidth(), getWorldHeight());

        // test that shows the CSV file is read into the arraylist correctly
        GameApp.drawText("basic", ingredientsList.get(3).name,20,20, WHITE);

        GameApp.drawTexture("bowl", bowl.x, bowl.y, BOWL_SIZE, BOWL_SIZE);
        GameApp.endSpriteRendering();


    }


    public void handlePlayerInput(float delta) {

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
        GameApp.disposeFont("basic");
    }


    public ArrayList<Ingredient> readCSV(String filename){
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        CsvReader reader = new CsvReader(filename);
        reader.skipRow();
        reader.setSeparator(',');
        while(reader.loadRow()){
            Ingredient newIngredient = new Ingredient();
            newIngredient.name = reader.getString(0);
            newIngredient.filename = reader.getString(1);
            ingredients.add(newIngredient);

        }
        return ingredients;
    }


}
