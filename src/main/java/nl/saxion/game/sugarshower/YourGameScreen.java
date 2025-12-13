package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;
import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.util.ArrayList;
import java.util.Random;

public class YourGameScreen extends ScalableGameScreen {

    //GAME SETTINGS
    public static final int BOWL_SIZE = 75;
    public static final int BOWL_SPEED = 600;
    public static final int INGREDIENT_SIZE = 80;
    public static final float bgMusicVolume = 0.5f;
    public static final float soundVolume = 0.8f;

    //GAME OBJECTS
    Bowl bowl;
    ArrayList<Ingredient> ingredients;
    Random random;
    float spawnTimer;
    float spawnInterval = 1.0f; // -- Spawn every 1 second
    String lastSpawnedType = "";

    //CSV DATA
    static ArrayList<Recipe> recipesArrayList = readCSVRecipes("src/main/resources/recipes.csv");
    ArrayList<String> ingredientTypes = readCSVIngredients("src/main/resources/ingredients.csv");

    // LEVEL SYSTEM
    static int currentLevel = 1;
    ArrayList<String> neededIngredientsTemp;
    static ArrayList<String> neededIngredients;

    // -- (We'll implement later, I got a bug -- Wina :))
    // recipesArrayList.get(currentLevel).recipeIngredientList;

    ArrayList<String> caughtIngredients = new ArrayList<>();
    int countIngredientsCaught;

    static int lives = 3;
    int score = 0;

    //POPUP -- Memorize Recipe
    boolean memorizeMode = true;
    float memorizeTime = 3f;

    public YourGameScreen() {
        super(800, 800);
    }

    @Override
    public void show() {

        // Keeps track of the level
        //currentLevel = 1;

//        neededIngredients = new ArrayList<>(
//                recipesArrayList.get(currentLevel - 1).recipeIngredientList);
        setLevel(currentLevel);
//        neededIngredientsTemp = new ArrayList<String>(recipesArrayList.get(currentLevel - 1).recipeIngredientList);
//        neededIngredients = (ArrayList) neededIngredientsTemp.clone();


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
        GameApp.addTexture("banner", "textures/banner.png");
        GameApp.addTexture("customer_1", "textures/customer_1.png");

        GameApp.addTexture("life", "textures/life.png");
        GameApp.addTexture("background", "textures/pink-bg.png");
        GameApp.addTexture("speech_bubble", "textures/speech_bubble.png");
        GameApp.addFont("roboto", "fonts/Roboto_SemiBold.ttf", 20);

        // Load all ingredient images using helper method
        for (String ingredientName : ingredientTypes) {
            String texturePath = getTexturePath(ingredientName);
            GameApp.addTexture(ingredientName, texturePath);
        }

        //Load the images of all finished products
        for (Recipe recipe : recipesArrayList) {
            String texturePath = "textures/finished_products/" + recipe.name + ".png";
            GameApp.addTexture(recipe.name, texturePath);
        }

        //Load audio
        GameApp.addMusic("bg-music", "audio/The_Biggest_Smile.mp3");
        GameApp.addSound("correct caught", "audio/correct caught.ogg");
        GameApp.addSound("bad caught", "audio/Bottle Break.wav");

        //Play background music
        AudioControl.playMusic("bg-music", true, bgMusicVolume);


    }

    @Override
    public void render(float delta) {
        super.render(delta);

        //--------------------------- GAME LOGIC -------------------------------

        popUpRecipeTimer(delta);

        handlePlayerInput(delta);


        if (!memorizeMode) {
            spawnIngredients(delta);
            updateIngredients(delta);
            checkCollisions();
        }

        // Escape button to exit
        if (GameApp.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GameApp.quit();
        }

        if (lives <= 0) {
            GameApp.switchScreen("GameOverScreen");
            //Link to GameOver screen for now until we figure out the level system - Nhi
        }

        // Mute option
        if (GameApp.isKeyJustPressed(Input.Keys.M)) {
            AudioControl.toggleMuteMode();
            GameApp.getMusic("bg-music").setVolume(AudioControl.muteMode ? 0f : bgMusicVolume);
        }

        //--------------------------- GRAPHIC RENDERING -------------------------------

        GameApp.clearScreen();
        GameApp.startSpriteRendering();
        GameApp.drawTexture("background", 0, 0, getWorldWidth(), getWorldHeight());

        //Draw pop-up
        if (memorizeMode) {
            drawRecipePopup();
            GameApp.endSpriteRendering();
            return;
        }

        //Draw bowl
        GameApp.drawTexture("bowl", bowl.x, bowl.y, BOWL_SIZE, BOWL_SIZE);
        //GameApp.drawText("roboto", "lives: " + lives, 30, getWorldHeight() - 50, "black");


        //Draw banner in the centre
        GameApp.drawTexture("banner", getWorldWidth() / 7, getWorldHeight()-180, 600, 200);


        for (Recipe recipe : recipesArrayList) {
            if (recipe.level == currentLevel) {
                drawRecipes(currentLevel);
            }
        }
        //drawRecipes(currentLevel);


        //Draw images of falling items in their coordinates
        for (Ingredient ingredient : ingredients) {
            if (ingredient.active) {
                GameApp.drawTexture(ingredient.type, ingredient.x, ingredient.y, INGREDIENT_SIZE, INGREDIENT_SIZE);
            }
        }

        //Draw hearts to visualize player's lives
        drawLivesHearts(lives);

        //Draw information of current level and the level recipe
        GameApp.drawText("roboto", "Level:" + recipesArrayList.get(currentLevel - 1).level + " - " + recipesArrayList.get(currentLevel - 1).name, getWorldWidth() / (float) 2.6, getWorldHeight() - 70, "black");
        //GameApp.drawText("roboto", "Recipe to make: " + recipesArrayList.get(currentLevel - 1).name, 30, getWorldHeight() - 150, "black");
        GameApp.drawText("roboto", "Ingredients needed to make: " + neededIngredients.toString(), 30, getWorldHeight() - 200, "black");


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
            GameApp.drawText("roboto", " - " + ing.replace("_", " "), textX + 150, textY, "black");
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
        // Added some boundary space where the bowl can't go (100 each side)
        bowl.x = GameApp.clamp(bowl.x, 100, getWorldWidth() - (BOWL_SIZE+100));
    }

    private void spawnIngredients(float delta) {
        spawnTimer += delta;

        //speed depending on phases level1-5 is phase 1, level 5-10 is phase 2, level 10-15 is phase 3
        int ingredientSpeed = 200;

        if (currentLevel > 5) {
            ingredientSpeed += 100;
        }
        if (currentLevel > 10) {
            ingredientSpeed += 100;
        }

        //Decrese spawning interval by 0.03 per level.
        //Add limit to adjust levels not to go too fast.
        float adjustedSpawnInterval = spawnInterval;
        adjustedSpawnInterval -= currentLevel * 0.03f;

        if (adjustedSpawnInterval < 0.4f) {
            adjustedSpawnInterval = 0.4f;
        }


        //Control the diversity of falling objects
        if (spawnTimer >= adjustedSpawnInterval) {
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

            //Prevent too many duplicate items falling at same time
            if (lastSpawnedType != null) {
                while (randomType.equals(lastSpawnedType)) {
                    randomType = ingredientTypes.get(
                            SaxionApp.getRandomValueBetween(0, ingredientTypes.size())
                    );
                }

                lastSpawnedType = randomType;
            }

            Ingredient newIngredient = new Ingredient(randomType, ingredientSpeed + random.nextInt(100));

            // Added some boundaries for the ingredients to fall below the banner and with some buffer space to the sides
            newIngredient.x = random.nextInt(100,(int) getWorldWidth() - (INGREDIENT_SIZE+100));
            newIngredient.y = getWorldHeight()-200;
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

                //lose a life if wrong ingredient.
                if (!neededIngredients.contains(ingredient.type)) {

                    if (ingredient.type.equals("life") && lives < 3) {
                        lives++;
                    } else if (ingredient.type.contains("rotten")) {
                        lives = lives - 2;
                    } else if (!ingredient.type.equals("life")) {
                        AudioControl.playSound("bad caught", soundVolume);
                        System.out.println("wrong!!! -1 life");
                        lives--;
                    }

                }

                // Check if caught ingredient is in the level's needed ingredient list
                // removes the ingredient from the list if it matches
                if (neededIngredients.contains(ingredient.type)) {
                    neededIngredients.remove(ingredient.type);
                    AudioControl.playSound("correct caught", soundVolume);
                }


                // Once all the ingredients have been caught it raises the level and creates the next
                // neededIngredients list
                if (neededIngredients.isEmpty()) {
                    currentLevel++;
                    setLevel(currentLevel);
//                    ArrayList<String> neededIngredientsTemp = new ArrayList<>(recipesArrayList.get(currentLevel - 1).recipeIngredientList);
//                    neededIngredients = (ArrayList) neededIngredientsTemp.clone();

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
        GameApp.disposeTexture("speech_bubble");
        GameApp.disposeTexture("life");
        GameApp.disposeTexture("banner");
        GameApp.disposeTexture("customer_1");
        //clean up audio
        GameApp.disposeMusic("bg-music");
        GameApp.disposeSound("correct caught");
        GameApp.disposeSound("bad caught");


        // Clean up ingredient textures
        for (String type : ingredientTypes) {
            GameApp.disposeTexture(type);
        }

        for (Recipe recipe : recipesArrayList) {
            if (recipe.level != currentLevel) {
                GameApp.disposeTexture(recipe.name);
            }
        }

        for (Recipe recipe : recipesArrayList) {
            GameApp.disposeTexture(recipe.name);
        }


        ingredients.clear();
    }

    // Read the ingredients.csv into an arraylist
    public ArrayList<String> readCSVIngredients(String filename) {
        ArrayList<String> ingredients = new ArrayList<>();
        CsvReader reader = new CsvReader(filename);
        reader.skipRow();
        reader.setSeparator(',');
        while (reader.loadRow()) {
            ingredients.add(reader.getString(0).trim());
        }
        return ingredients;
    }

    // helper method
    public String getTexturePath(String ingredientName) {

        return "textures/ingredients/" + ingredientName + ".png";
    }


    // Read the recipes.csv into an arraylist of Recipes
    public static ArrayList<Recipe> readCSVRecipes(String filename) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        CsvReader reader = new CsvReader(filename);
        reader.skipRow();
        reader.setSeparator(',');
        while (reader.loadRow()) {
            Recipe newRecipe = new Recipe();
            newRecipe.phase = (reader.getInt(0));
            newRecipe.level = (reader.getInt(1));
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

    public static int getCurrentLevel() {
        return currentLevel;
    }


    public static void setLevel(int currentLevel) {
        ArrayList<String> neededIngredientsTemp = new ArrayList<>(recipesArrayList.get(currentLevel - 1).recipeIngredientList);
        neededIngredients = (ArrayList) neededIngredientsTemp.clone();
        lives = 3;
        System.out.println("set: current level: " + currentLevel);


    }

    //draw textures for the three lives
    public static void drawLivesHearts(int lives) {
        float x = GameApp.getWorldWidth() / (float) 2.4;
        for (int i = 1; i <= lives; i++) {
            GameApp.drawTexture("life", x, GameApp.getWorldHeight() - 130, BOWL_SIZE - 20, BOWL_SIZE - 20);
            x += 45;


        }
    }

    public static void drawRecipes(int currentLevel) {

        //Draw bubble with image of finished product (ordered item)
        GameApp.drawTexture("speech_bubble", GameApp.getWorldWidth()-150, GameApp.getWorldHeight()-200, BOWL_SIZE + 50, BOWL_SIZE + 50);
        //Draw a customer
        GameApp.drawTexture("customer_1", GameApp.getWorldWidth()-100, GameApp.getWorldHeight()-260, BOWL_SIZE , BOWL_SIZE );

        GameApp.drawTexture(recipesArrayList.get(currentLevel - 1).name, GameApp.getWorldWidth()-120, GameApp.getWorldHeight()-170, INGREDIENT_SIZE, INGREDIENT_SIZE);


    }

}




