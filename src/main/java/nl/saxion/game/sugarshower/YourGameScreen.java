package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;
import nl.saxion.app.CsvReader;

import java.util.ArrayList;
import java.util.Random;

public class YourGameScreen extends ScalableGameScreen {

    // LEVEL SYSTEM
    static int currentLevel = 1;
    ArrayList<String> neededIngredientsTemp;
    static ArrayList<String> neededIngredients;

    //GAME SETTINGS
    public static final int BOWL_HEIGHT = 110;
    public static final int BOWL_WIDTH = 85;
    public static final int BOWL_SPEED = 600;
    public static final int INGREDIENT_SIZE = 65;
    public static final float bgMusicVolume = 0.5f;
    public static final float soundVolume = 0.8f;

    //UI SCALING CONSTANTS (adjust these to get the right size)
    public static final float HEART_SIZE = 50f;         // Original: 200x170 → scale to 40x34
    public static final float QUESTION_MARK_SIZE = 40f;
    public static final float BUBBLE_WIDTH = 120f;      // Original: 340x300 → scale to 150x132
    public static final float BUBBLE_HEIGHT = 102f;
    public static final float CUSTOMER_SIZE = 150f;     // Make customer bigger
    public static final float ORDER_SIZE = 105f;         // Finished product in bubble

    //GAME OBJECTS
    Bowl bowl;
    ArrayList<Ingredient> ingredients;
    Random random;
    float spawnTimer;
    float spawnInterval = 1.0f;
    String lastSpawnedType = "";

    //CSV DATA
    static ArrayList<Recipe> recipesArrayList = readCSVRecipes("src/main/resources/recipes.csv");
    ArrayList<String> ingredientTypes = readCSVIngredients("src/main/resources/ingredients.csv");

    // -- (We'll implement later, I got a bug -- Wina :))
    // recipesArrayList.get(currentLevel).recipeIngredientList;

    ArrayList<String> caughtIngredients = new ArrayList<>();
    int countIngredientsCaught;

    static int lives = 3;
    int maxLives = 3;

    //POPUP -- Memorize Recipe
    boolean memorizeMode = true;
    float memorizeTime = 3f;

    // CUSTOMER SYSTEM
    private int currentCustomer = 0;
    private int previousCustomer = -1;
    private int totalCustomers = 8; // Adjust based on how many customer images you have

    public YourGameScreen() {
        super(800, 800);
    }

    @Override
    public void show() {

        setLevel(currentLevel);

        // clear ing for the new level
        caughtIngredients.clear();

        //Pop-up setup
        memorizeMode = true;
        memorizeTime = 3f;
        GameApp.addTexture("popup_gui", "textures/popupscreen.png");

        //BOWL
        bowl = new Bowl();
        bowl.x = getWorldWidth() / 2 - (float) BOWL_HEIGHT / 2;
        bowl.y = 15;

        //INGREDIENT SYSTEM
        ingredients = new ArrayList<>();
        random = new Random();
        spawnTimer = 0;
        countIngredientsCaught = 0;

        //ArrayList<Recipe> recipesArrayList = readCSVRecipes("src/main/resources/recipes.csv");

        // TEXTURES
        GameApp.addTexture("bowl", "textures/bowl.png");
        GameApp.addTexture("life", "textures/life.png");
        GameApp.addTexture("background", "textures/playingbg.png");
        GameApp.addTexture("speech_bubble", "textures/text_bubble.png");
        GameApp.addTexture("circle_bubble", "textures/circle_bubble.png");

        // Hearts textures
        GameApp.addTexture("heart_full", "textures/heart_full.png");
        GameApp.addTexture("heart_empty", "textures/heart_empty.png");

        // Questionmark textures
        GameApp.addTexture("question_mark", "textures/questionmark.png");


        // Load all customer textures
        for (int i = 0; i < totalCustomers; i++) {
            GameApp.addTexture("customer_" + i, "textures/customers/customer_" + i + ".png");
        }

        // Select random customer for this level
        selectRandomCustomer();

        // Fonts
        GameApp.addFont("roboto", "fonts/Roboto_SemiBold.ttf", 20);
        GameApp.addFont("bubble", "fonts/bubble.ttf", 40);
        GameApp.addFont("bubble_small", "fonts/bubble.ttf", 25);
        GameApp.addFont("bubble_lists", "fonts/bubble.ttf", 15);
        GameApp.addFont("bubble_count", "fonts/bubble.ttf", 12);
        GameApp.addColor("customLine", 64, 15, 38);

        // Load all ingredient images
        for (String ingredientName : ingredientTypes) {
            String texturePath = getTexturePath(ingredientName);
            GameApp.addTexture(ingredientName, texturePath);
        }

        //Load finished products
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

    /**
     * Select a random customer that's different from the previous one
     */
    private void selectRandomCustomer() {
        int newCustomer;
        do {
            newCustomer = random.nextInt(totalCustomers);
        } while (newCustomer == previousCustomer && totalCustomers > 1);

        previousCustomer = currentCustomer;
        currentCustomer = newCustomer;
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
            GameApp.switchScreen("GameOverScreen"); //quit() method ends the game too abruptly.
        }
        // Press V for Victory screen. For testing. Delete later.
        if (GameApp.isKeyJustPressed(Input.Keys.V)) {
            GameApp.switchScreen("VictoryScreen"); //quit() method ends the game too abruptly.
        }

        if (lives <= 0) {
            GameApp.switchScreen("GameOverScreen");
            //Link to GameOver screen for now until we figure out the level system - Nhi
        }
        if (currentLevel > 15) {
            GameApp.switchScreen("VictoryScreen");
            //Link to GameOver screen for now until we figure out the level system - Nhi
        }


        // Press M to mute audio
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
        GameApp.drawTexture("bowl", bowl.x, bowl.y, BOWL_WIDTH, BOWL_HEIGHT);


        //Draw falling ingredients
        for (Ingredient ingredient : ingredients) {
            if (ingredient.active) {
                GameApp.drawTexture(ingredient.type, ingredient.x, ingredient.y,
                        INGREDIENT_SIZE, INGREDIENT_SIZE);
            }
        }

        //just for debugging - get rid of later
//        GameApp.drawTextHorizontallyCentered("bubble_count", String.valueOf(neededIngredients),
//                300, 500, "customLine");


        // ===== NEW UI LAYOUT =====
        // Draw all top UI elements
        if (currentLevel <= recipesArrayList.size()) {
            drawTopUI();
        }


        GameApp.endSpriteRendering();
    }

    /**
     * Draw all UI elements at the top of the screen
     */
    private void drawTopUI() {
        // ===== TOP LEFT: Customer + Speech Bubble + Order =====
        drawCustomerWithOrder();

        // ===== TOP CENTER: Current Level =====
        drawLevelInfo();

        // ===== TOP RIGHT: Hearts (Lives) =====
        drawLivesHearts(lives);
        drawProgress(recipesArrayList, caughtIngredients);
    }

    /**
     * Draw customer face with speech bubble and order image
     * Position: Top Left Corner
     */
    private void drawCustomerWithOrder() {
        float customerX = 0;
        float customerY = getWorldHeight() - CUSTOMER_SIZE;

        float bubbleX = customerX + CUSTOMER_SIZE; // Next to customer
        float bubbleY = getWorldHeight() - BUBBLE_HEIGHT - 20;

        // Calculate center of bubble for the order
        float orderX = (bubbleX + (BUBBLE_WIDTH - ORDER_SIZE) / 2) + 4;
        float orderY = bubbleY + (BUBBLE_HEIGHT - ORDER_SIZE) / 2;

        // Draw customer face (bigger)
        String customerTexture = "customer_" + currentCustomer;
        GameApp.drawTexture(customerTexture, customerX, customerY, CUSTOMER_SIZE, CUSTOMER_SIZE);

        // Draw speech bubble (scaled down)
        GameApp.drawTexture("speech_bubble", bubbleX, bubbleY, BUBBLE_WIDTH, BUBBLE_HEIGHT);

        // Draw order (finished product) centered in bubble
        if (currentLevel < 16) {
            String orderTexture = recipesArrayList.get(currentLevel - 1).name;
            GameApp.drawTexture(orderTexture, orderX, orderY, ORDER_SIZE, ORDER_SIZE);

        }

    }

    /**
     * Draw current level number and recipe name
     * Position: Top Center
     */
    private void drawLevelInfo() {
        float centerX = getWorldWidth() / 2;
        float textY = getWorldHeight() - 30;

        // Level number
        String levelText = "Lv." + currentLevel;
        GameApp.drawTextHorizontallyCentered("bubble_small", levelText,
                centerX, textY, "customLine");

        // Recipe name below level
        String recipeName = recipesArrayList.get(currentLevel - 1).name.replace("_", " ");
        GameApp.drawTextHorizontallyCentered("roboto", recipeName,
                centerX, textY - 30, "customLine");
    }

    /**
     * Draw hearts showing current lives
     * Position: Top Right Corner
     * Shows full hearts for remaining lives, empty hearts for lost lives
     */
    private void drawLivesHearts(int lives) {
        float spacing = 10;

        // Calculate total width of hearts to center them in top right
        float totalWidth = (HEART_SIZE * maxLives) + (spacing * (maxLives - 1));
        float startX = getWorldWidth() - totalWidth - 20; // 20px padding from right edge
        float heartY = getWorldHeight() - HEART_SIZE - 20; // 20px padding from top

        for (int i = 0; i < maxLives; i++) {
            float heartX = startX + (i * (HEART_SIZE + spacing));

            // Draw full heart if player still has this life, empty heart otherwise
            if (i < lives) {
                GameApp.drawTexture("heart_full", heartX, heartY, HEART_SIZE, HEART_SIZE * 0.85f);
            } else {
                GameApp.drawTexture("heart_empty", heartX, heartY, HEART_SIZE, HEART_SIZE * 0.85f);
            }
        }
    }

    private void drawRecipePopup() {
        float popupW = getWorldWidth();
        float popupH = getWorldHeight();
        float popupX = getWorldWidth() / 2 - popupW / 2;
        float popupY = getWorldHeight() / 2 - popupH / 2;

        //Booklet texture
        GameApp.drawTexture("popup_gui", popupX, popupY, popupW, popupH);
        GameApp.drawText("bubble", "Memorize"+"\n"+"this recipe! ", getWorldWidth() / 4, popupH - 100, "customLine");


        // Counter texture inside speech bubble - use scaled bubble
        float counterBubbleX = popupW / 15;
        float counterBubbleY = popupH - 220;
        GameApp.drawTexture("circle_bubble", counterBubbleX, counterBubbleY, 100, 88); // Scaled: 100x88
        GameApp.drawText("bubble", String.valueOf((int) memorizeTime), (popupW / 9), popupH - 190, "customLine");

        // Name of the current recipe texture
        GameApp.drawText("bubble_small", recipesArrayList.get(currentLevel - 1).name.replace("_", "\n"),
                80, 550, "customLine");

        // ===== LAYOUT =====
        // Left side: Big finished product picture
        float bigPicX = 81;
        float bigPicY = 285;

        // Right side: Ingredient list - TWO COLUMNS
        float listStartX = 450;
        float listY = getWorldHeight() - 305;
        float ingredientSize = 50;
        float rowHeight = 75;
        float columnSpacing = 135; // Space between columns

        // Draw big finished product picture
        GameApp.drawTexture(recipesArrayList.get(currentLevel - 1).name, bigPicX, bigPicY, 254, 232);

        float currentX = listStartX;
        float currentY = listY;

        int ingredientCounter = 0;
        int itemsPerColumn = 4; // Show 4 items per column

        for (String ingredient : recipesArrayList.get(currentLevel - 1).recipeIngredientList) {
            // Move to second column after 4 items
            if (ingredientCounter == itemsPerColumn) {
                currentX += columnSpacing;
                currentY = listY;
            }

            // Draw ingredient icon (small)
            GameApp.drawTexture(ingredient, currentX - 15, currentY - 20, ingredientSize, ingredientSize);

            // Draw ingredient name next to the icon
            String ingredientText = ingredient.replace("_", " ");
            GameApp.drawText("bubble_lists", ingredientText, currentX + ingredientSize - 10,
                    currentY + 5, "customLine");
            GameApp.drawText("bubble_count", "x1", currentX + ingredientSize - 10,
                    currentY - 10, "customLine");

            ingredientCounter++;
            currentY -= rowHeight;
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

        bowl.y = GameApp.clamp(bowl.y, 0, getWorldHeight() - BOWL_WIDTH);
        bowl.x = GameApp.clamp(bowl.x, 0, getWorldWidth() - (BOWL_HEIGHT));
    }

    /**
     * Adjust speed and amount of ingredients that fall
     */
    private void spawnIngredients(float delta) {
        spawnTimer += delta;


        int ingredientSpeed = 200;
        if (currentLevel > 5) ingredientSpeed += 100;
        if (currentLevel > 10) ingredientSpeed += 100;

        // Adjust spawn interval by level
        float adjustedSpawnInterval = spawnInterval - (currentLevel * 0.03f);
        if (adjustedSpawnInterval < 0.4f) adjustedSpawnInterval = 0.4f;


        if (spawnTimer >= adjustedSpawnInterval) {
            // Determine amount ingredients to spawn based on phase
            int minIngredientsPerSpawn = 1;
            int maxIngredientsPerSpawn = 2;

            if (currentLevel > 5) {
                minIngredientsPerSpawn = 2;
                maxIngredientsPerSpawn = 3;
            }
            if (currentLevel > 10) {
                minIngredientsPerSpawn = 2;
                maxIngredientsPerSpawn = 4;
            }

            int numToSpawn = random.nextInt(maxIngredientsPerSpawn - minIngredientsPerSpawn + 1)
                    + minIngredientsPerSpawn;

            System.out.println("Spawning " + numToSpawn + " ingredients");

            for (int i = 0; i < numToSpawn; i++) {
                spawnSingleIngredient(ingredientSpeed);
            }

            spawnTimer = 0;
        }
    }

    /**
     * Adjust every falling ingredients to avoid duplicated types and overlapping position
     */
    private void spawnSingleIngredient(int baseSpeed) {
        String randomType;

        if (random.nextFloat() < 0.6f && !neededIngredients.isEmpty()) {
            ArrayList<String> stillNeeded = new ArrayList<>(neededIngredients);
            randomType = stillNeeded.get(random.nextInt(stillNeeded.size()));
        } else {
            randomType = getRandomIngredientType();
        }

        if (lastSpawnedType != null) {
            while (randomType.equals(lastSpawnedType)) {
                randomType = getRandomIngredientType();
            }
            lastSpawnedType = randomType;
        }

        //===Avoid overlaps===
        Ingredient newIngredient = new Ingredient(randomType, baseSpeed + random.nextInt(100));

        int attempts = 0;
        boolean positionFound = false;

        while (!positionFound && attempts < 10) {
            newIngredient.x = random.nextInt(100, (int) getWorldWidth() - (INGREDIENT_SIZE + 100));

            boolean tooClose = false;
            for (Ingredient existing : ingredients) {
                if (existing.active) {
                    float distance = Math.abs(existing.x - newIngredient.x);
                    if (distance < 100) {
                        tooClose = true;
                        break;
                    }
                }
            }

            if (!tooClose) {
                positionFound = true;
            }
            attempts++;
        }

        // If no good position found after 10 attempts, use the last random position anyway
        newIngredient.y = getWorldHeight() - 200;
        ingredients.add(newIngredient);
    }


    private boolean canGainLife() {
        return lives < maxLives;
    }

    private String getRandomIngredientType() {
        ArrayList<String> possibleIngredients = new ArrayList<>(ingredientTypes);
        if (!canGainLife()) {
            possibleIngredients.remove("life");
        }
        return possibleIngredients.get(random.nextInt(possibleIngredients.size()));
    }


    private void updateIngredients(float delta) {
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);

            if (ingredient.active) {
                ingredient.y -= ingredient.speed * delta;

                if (ingredient.y < -INGREDIENT_SIZE) {
                    ingredients.remove(i);
                }
            }
        }
    }

    private void checkCollisions() {
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);

            if (ingredient.active && isColliding(ingredient, bowl)) {
                ingredient.active = false;
                System.out.println("Collected: " + ingredient.type);

                if (!neededIngredients.contains(ingredient.type)) {
                    if (ingredient.type.equals("life") && lives < 3) {
                        lives++;
                    } else if (ingredient.type.equals("bomb")) {
                        AudioControl.playSound("bad caught", soundVolume);
                        lives = 0;
                    } else if (ingredient.type.contains("rotten")) {
                        lives = lives - 2;
                    } else if (!ingredient.type.equals("life")) {
                        AudioControl.playSound("bad caught", soundVolume);
                        lives--;
                    }


                } else {
                    neededIngredients.remove(ingredient.type);
                    caughtIngredients.add(ingredient.type);
                    AudioControl.playSound("correct caught", soundVolume);
                }

                ingredients.remove(i);

                if (neededIngredients.isEmpty()) {
                    Recipe completedRecipe = recipesArrayList.get(currentLevel - 1);
                    LevelCompleteScreen.setCompletedRecipe(completedRecipe, caughtIngredients);


                    currentLevel++;

                    if (currentLevel - 1 >= recipesArrayList.size()) {
                        GameApp.switchScreen("VictoryScreen");
                        //Link to GameOver screen for now until we figure out the level system - Nhi
                    }
                    if (currentLevel < recipesArrayList.size()) {
                        selectRandomCustomer();

                    }
                    // Select new customer for next level


                    if (currentLevel - 1 > recipesArrayList.size()) {
                        GameApp.switchScreen("VictoryScreen");
                        return;
                    }


                    GameApp.switchScreen("LevelCompleteScreen");
                    return;
                }

                break;
            }
        }
    }

    private boolean isColliding(Ingredient ingredient, Bowl bowl) {
        float widthPadding = 5f;
        float heightPadding = 15f;

        return ingredient.x + widthPadding < bowl.x + BOWL_WIDTH - widthPadding &&
                ingredient.x + INGREDIENT_SIZE - widthPadding > bowl.x + widthPadding &&
                ingredient.y + heightPadding < bowl.y + BOWL_WIDTH - heightPadding &&
                ingredient.y + INGREDIENT_SIZE - heightPadding > bowl.y + heightPadding;
    }

    @Override
    public void hide() {
        GameApp.disposeTexture("bowl");
        GameApp.disposeTexture("background");
        GameApp.disposeTexture("popup_gui");
        GameApp.disposeTexture("speech_bubble");
        GameApp.disposeTexture("life");
        GameApp.disposeTexture("heart_full");
        GameApp.disposeTexture("heart_empty");
        GameApp.disposeTexture("question_mark");
        GameApp.disposeTexture("circle_bubble");

        // Dispose all customer textures
        for (int i = 0; i < totalCustomers; i++) {
            GameApp.disposeTexture("customer_" + i);
        }

        GameApp.disposeFont("roboto");
        GameApp.disposeFont("bubble");
        GameApp.disposeFont("bubble_small");
        GameApp.disposeFont("bubble_lists");
        GameApp.disposeFont("bubble_count");

        GameApp.disposeMusic("bg-music");
        GameApp.disposeSound("correct caught");
        GameApp.disposeSound("bad caught");

        for (String type : ingredientTypes) {
            GameApp.disposeTexture(type);
        }

        for (Recipe recipe : recipesArrayList) {
            GameApp.disposeTexture(recipe.name);
        }

        ingredients.clear();
    }

    // CSV methods remain the same...
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

    public String getTexturePath(String ingredientName) {
        return "textures/ingredients/" + ingredientName + ".png";
    }

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

    // Draw progress bar for caught ingredients
    private void drawProgress(ArrayList<Recipe> recipesArrayList, ArrayList<String> caughtIngredients) {
        float spacing = 5;
        int length = recipesArrayList.get(currentLevel - 1).recipeIngredientList.size();
        float startX = getWorldWidth() - QUESTION_MARK_SIZE - 20;
        float progressY = getWorldHeight() - QUESTION_MARK_SIZE * 3; // 20px padding from top

        for (int i = 0; i < length; i++) {
            float progressX = startX - (i * (QUESTION_MARK_SIZE + spacing));

            //Draw question marks if caught ingredients list is empty, draw ingredient otherwise
            if (i < caughtIngredients.size()) {
                GameApp.drawTexture(caughtIngredients.get(i), progressX, progressY, QUESTION_MARK_SIZE, QUESTION_MARK_SIZE * 0.85f);
            } else {
                GameApp.drawTexture("question_mark", progressX, progressY, QUESTION_MARK_SIZE, QUESTION_MARK_SIZE * 0.85f);
            }
        }
    }


}