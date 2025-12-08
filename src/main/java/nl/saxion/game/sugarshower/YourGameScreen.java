package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;
import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;
import java.util.ArrayList;
import java.util.Random;

public class YourGameScreen extends ScalableGameScreen {

    //GAME SETTINGS
    public static final int BOWL_SIZE = 100;
    public static final int BOWL_SPEED = 500;
    public static final int INGREDIENT_SIZE = 80;

    //GAME OBJECTS
    Bowl bowl;
    ArrayList<Ingredient> ingredients;
    Random random;

    float spawnTimer;
    float spawnInterval = 1.0f; // -- Spawn every 1 second

    //CSV DATA

    ArrayList<Recipe> recipesArrayList = readCSVRecipes("src/main/resources/recipes.csv");
    ArrayList<String> ingredientTypes = readCSVIngredients("src/main/resources/ingredients.csv");

    // LEVEL SYSTEM
    int currentLevel;
    ArrayList<String> neededIngredients; // -- (We'll implement later, I got a bug -- Wina :))
    // recipesArrayList.get(currentLevel).recipeIngredientList;

    ArrayList<String> caughtIngredients = new ArrayList<>();
    int countIngredientsCaught;

    int lives = 3;
    int score = 0;

    //POPUP -- Memorize Recipe
    boolean memorizeMode = true;
    float memorizeTime = 3f;

    public YourGameScreen() {
        super(800, 800);
    }

    @Override
    public void show() {
        currentLevel = 1; // Keeps track of the level

        neededIngredients = new ArrayList<>(
                recipesArrayList.get(currentLevel - 1).recipeIngredientList);

        //Pop-up setup.
        memorizeMode = true;
        memorizeTime = 3f;
        GameApp.addTexture("popup_gui", "textures/recipe_popup.png");

        //BOWL
        bowl = new Bowl();
        bowl.x = getWorldWidth() / 2 - (float) BOWL_SIZE / 2;
        bowl.y = 50; // Position bowl at the bottom

        //INGREDIENT SYSTEM
        ingredients = new ArrayList<>();
        random = new Random();
        spawnTimer = 0;
        countIngredientsCaught = 0;

        //ArrayList<Recipe> recipesArrayList = readCSVRecipes("src/main/resources/recipes.csv");

        // TEXTURES
        GameApp.addTexture("bowl", "textures/bowl_03.png");
        GameApp.addTexture("background", "textures/pink-bg.png");
        GameApp.addFont("roboto", "fonts/Roboto_SemiBold.ttf", 20);

        // Load all ingredient images using helper method
        for (String ingredientName : ingredientTypes) {
            String texturePath = getTexturePath(ingredientName);
            GameApp.addTexture(ingredientName, texturePath);
        }


    }

    @Override
    public void render(float delta) {
        super.render(delta);

        popUpRecipeTimer(delta);

        handlePlayerInput(delta);

        if (!memorizeMode) {
            spawnIngredients(delta);
            updateIngredients(delta);
            checkCollisions();
        }

        // Escape button to exit
        if (GameApp.isKeyJustPressed(Input.Keys.ESCAPE)){
            GameApp.switchScreen("GameOverScreen");
            //Link to GameOver screen for now until we figure out the level system - Nhi
        }

        // Draw elements
        GameApp.clearScreen();
        GameApp.startSpriteRendering();
        GameApp.drawTexture("background", 0, 0, getWorldWidth(), getWorldHeight());

        if (memorizeMode) {
            drawRecipePopup();
            GameApp.endSpriteRendering();
            return;
        }

        for (Ingredient ingredient : ingredients) {
            if (ingredient.active) {
                GameApp.drawTexture(ingredient.type, ingredient.x, ingredient.y, INGREDIENT_SIZE, INGREDIENT_SIZE);
            }
        }

        GameApp.drawTexture("bowl", bowl.x, bowl.y, BOWL_SIZE, BOWL_SIZE);
        GameApp.drawText("roboto", "score: " + countIngredientsCaught, 30,getWorldHeight()-50,"black");

        // print the current level and the level recipe
        GameApp.drawText("roboto", "Current level:" + recipesArrayList.get(currentLevel-1).level, 30,getWorldHeight()-100,"black");
        GameApp.drawText("roboto", "Recipe to make: " + recipesArrayList.get(currentLevel-1).name, 30,getWorldHeight()-150,"black");
        //GameApp.drawText("roboto", "Ingredients needed to make: " + recipesArrayList.get(currentLevel-1).name + " " +  recipesArrayList.get(currentLevel-1).recipeIngredientList.toString() , 30,getWorldHeight()-200,"black");

        GameApp.endSpriteRendering();

    }

    private void drawRecipePopup() {
        float popupW = 600;
        float popupH = 400;
        float popupX = getWorldWidth() / 2 - popupW / 2;
        float popupY = getWorldHeight() / 2 - popupH / 2;

        GameApp.drawTexture("popup_gui", popupX, popupY, popupW, popupH);

        GameApp.drawTextHorizontallyCentered("roboto", "MEMORIZE THIS RECIPE!",
                getWorldWidth() / 2, popupY + popupH - 40, "black");

        GameApp.drawTextHorizontallyCentered("roboto",
                recipesArrayList.get(currentLevel - 1).name,
                getWorldWidth() / 2, popupY + popupH - 90, "black");

        float textX = popupX + 40;
        float textY = popupY + popupH - 140;

        for (String ing : neededIngredients) {
            GameApp.drawText("roboto", "- " + ing, textX+150, textY, "black");
            textY -= 30;
        }
    }

    private void popUpRecipeTimer(float delta) {
        if (memorizeMode) {
            memorizeTime -= delta;
            if (memorizeTime <= 0) {
                memorizeMode = false;
            }
        }
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
            String randomType;

            // 60% chance to spawn needed ingredient, 40% chance random
            if (random.nextFloat() < 0.6f && !neededIngredients.isEmpty()) {
                // Spawn a needed ingredient (that hasn't been caught yet)
                ArrayList<String> stillNeeded = new ArrayList<>(neededIngredients);

                if (!stillNeeded.isEmpty()) {
                    randomType = stillNeeded.get(random.nextInt(stillNeeded.size()));
                } else {
                    randomType = ingredientTypes.get(SaxionApp.getRandomValueBetween(0, ingredientTypes.size()));
                }
            } else {
                // Spawn any random ingredient
                randomType = ingredientTypes.get(SaxionApp.getRandomValueBetween(0, ingredientTypes.size()));
            }

            Ingredient newIngredient = new Ingredient(randomType, 100 + random.nextInt(100));
            newIngredient.x = random.nextInt((int)getWorldWidth() - INGREDIENT_SIZE);
            newIngredient.y = getWorldHeight();
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

                // Check if caught ingredient is in the level's needed ingredient list
                // removes the ingredient from the list if it matches
                if (neededIngredients.contains(ingredient.type)){
                    neededIngredients.remove(ingredient.type);
                }
                // Once all the ingredients have been caught it raises the level and creates the next
                // neededIngredients list
                if (neededIngredients.isEmpty()){
                    currentLevel++;
                    neededIngredients = new ArrayList<>(recipesArrayList.get(currentLevel - 1).recipeIngredientList);

                    memorizeMode = true;
                    memorizeTime = 3f;

                }
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
        GameApp.disposeTexture("roboto");


        // Clean up ingredient textures
        for (String type : ingredientTypes) {
            GameApp.disposeTexture(type);
        }

        ingredients.clear();
    }

    // Read the ingredients.csv into an arraylist
    public ArrayList<String> readCSVIngredients(String filename){
        ArrayList<String> ingredients = new ArrayList<>();
        CsvReader reader = new CsvReader(filename);
        reader.skipRow();
        reader.setSeparator(',');
        while(reader.loadRow()){
            ingredients.add(reader.getString(0).trim());
        }
        return ingredients;
    }

    // helper method
    public String getTexturePath(String ingredientName) {

        return "textures/ingredients/" + ingredientName + ".png";
    }

    // Read the recipes.csv into an arraylist of Recipes
    public ArrayList<Recipe> readCSVRecipes(String filename){
        ArrayList<Recipe> recipes = new ArrayList<>();
        CsvReader reader = new CsvReader(filename);
        reader.skipRow();
        reader.setSeparator(',');
        while(reader.loadRow()){
            Recipe newRecipe = new Recipe();
            newRecipe.phase =(reader.getInt(0));
            newRecipe.level =(reader.getInt(1));
            newRecipe.name = (reader.getString(2));

            newRecipe.recipeIngredientList.add(reader.getString(3));
            newRecipe.recipeIngredientList.add(reader.getString(4));
            newRecipe.recipeIngredientList.add(reader.getString(5));
            newRecipe.recipeIngredientList.add(reader.getString(6));
            newRecipe.recipeIngredientList.add(reader.getString(7));
            newRecipe.recipeIngredientList.add(reader.getString(8));
            newRecipe.recipeIngredientList.add(reader.getString(9));
            newRecipe.recipeIngredientList.add(reader.getString(10));

            //remove the ingredients called NULL
            newRecipe.recipeIngredientList.removeIf(ingredient -> ingredient.equals("NULL"));

            recipes.add(newRecipe);
        }
        return recipes;
    }


}
