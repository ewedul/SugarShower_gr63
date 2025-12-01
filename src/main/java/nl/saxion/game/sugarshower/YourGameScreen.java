package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;
import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;
import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;
import java.util.ArrayList;
import java.util.Random;

import java.util.ArrayList;

import static com.badlogic.gdx.graphics.Color.WHITE;

public class YourGameScreen extends ScalableGameScreen {

    public static final int BOWL_SIZE = 150;
    public static final int BOWL_SPEED = 500;
    public static final int INGREDIENT_SIZE = 80;
    Bowl bowl;

    // Variables for falling ingredients
    ArrayList<Ingredient> ingredients;
    Random random;
    float spawnTimer;
    float spawnInterval = 1.0f; // Spawn every 1 second

    // Available ingredients
    ArrayList<String> ingredientTypes = readCSV("src/main/resources/ingredients.csv");

    // Variables for correction ingredients
    ArrayList<String> neededIngredients = new ArrayList<>();
    ArrayList<String> caughtIngredients = new ArrayList<>();
    int lives = 3;
    int score = 0;

    public YourGameScreen() {
        super(800, 800);
    }


    @Override
    public void show() {
        bowl = new Bowl();
        bowl.x = getWorldWidth() / 2 - BOWL_SIZE / 2;
        bowl.y = 50; // Position bowl at the bottom

        ingredients = new ArrayList<>();
        random = new Random();
        spawnTimer = 0;

        // Load textures
        GameApp.addTexture("bowl", "textures/bowl_03.png");
        GameApp.addTexture("background", "textures/pink-bg.png");

        // Load ingredient textures using helper method
        for (String ingredientName : ingredientTypes) {
            String texturePath = getTexturePath(ingredientName);
            GameApp.addTexture(ingredientName, texturePath);
        }


    }

    @Override
    public void render(float delta) {

        super.render(delta);

        handlePlayerInput(delta);
        updateIngredients(delta);
        spawnIngredients(delta);
        checkCollisions();

        // Draw ingredients
        if (GameApp.isKeyJustPressed(Input.Keys.ESCAPE)){
            GameApp.switchScreen("MainMenuScreen");

        }

        // Draw elements
        GameApp.clearScreen();
        GameApp.startSpriteRendering();
        GameApp.drawTexture("background", 0, 0, getWorldWidth(), getWorldHeight());

        for (Ingredient ingredient : ingredients) {
            if (ingredient.active) {
                GameApp.drawTexture(ingredient.type, ingredient.x, ingredient.y, INGREDIENT_SIZE, INGREDIENT_SIZE);
            }
        }

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

    private void spawnIngredients(float delta) {
        spawnTimer += delta;

        if (spawnTimer >= spawnInterval) {
            // Create new ingredient
            String randomType = ingredientTypes.get(SaxionApp.getRandomValueBetween(0,ingredientTypes.size()));
            Ingredient newIngredient = new Ingredient(randomType, 100 + random.nextInt(100)); // Speed between 100-200

            // Random x position
            newIngredient.x = random.nextInt((int)getWorldWidth() - INGREDIENT_SIZE);
            newIngredient.y = getWorldHeight(); // Start from top

            ingredients.add(newIngredient);
            spawnTimer = 0;
        }
    }

    private void updateIngredients(float delta) {
        // Update position of all active ingredients
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);

            if (ingredient.active) {
                ingredient.y -= ingredient.speed * delta;

                // Remove ingredients that fall off the bottom of the screen
                if (ingredient.y < -INGREDIENT_SIZE) {
                    ingredients.remove(i);
                }
            }
        }
    }


    private void checkCollisions() {
        for (Ingredient ingredient : ingredients) {
            if (ingredient.active && isColliding(ingredient, bowl)) {
                ingredient.active = false; // "Collect" the ingredient
                // Here you can add score or handle ingredient collection
                System.out.println("Collected: " + ingredient.type);
            }
        }
    }

    private boolean isColliding(Ingredient ingredient, Bowl bowl) {
        return ingredient.x < bowl.x + BOWL_SIZE &&
                ingredient.x + INGREDIENT_SIZE > bowl.x &&
                ingredient.y < bowl.y + BOWL_SIZE &&
                ingredient.y + INGREDIENT_SIZE > bowl.y;
    }



    @Override
    public void hide() {

        //clean up textures
        GameApp.disposeTexture("bowl");
        GameApp.disposeTexture("background");


        // Clean up ingredient textures
        for (String type : ingredientTypes) {
            GameApp.disposeTexture(type);
        }

        ingredients.clear();
    }

    // read the ingredients.csv into an arraylist
    public ArrayList<String> readCSV(String filename){
        ArrayList<String> ingredients = new ArrayList<>();
        CsvReader reader = new CsvReader(filename);
        reader.skipRow();
        reader.setSeparator(',');
        while(reader.loadRow()){
            ingredients.add(reader.getString(0));
        }
        return ingredients;
    }

    // helper method
    public String getTexturePath(String ingredientName) {
        return "textures/ingredients/" + ingredientName + ".png";
    }

}
