package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;
import nl.saxion.app.CsvReader;
import nl.saxion.app.SaxionApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InfiniteGameScreen extends ScalableGameScreen {

    // LEVEL SYSTEM - INFINITE MODE
    static int currentWave = 1;
    static int ordersCompleted = 0; // Track consecutive orders for difficulty scaling
    static ArrayList<String> neededIngredients;
    ArrayList<String> caughtIngredients = new ArrayList<>();
    static ArrayList<Recipe> currentOrders; // Multiple recipes per wave
    static HashMap<String, Integer> ingredientCounts; // Track how many of each ingredient needed
    static ArrayList<Integer> recentOrderIndices = new ArrayList<>(); // Track recent orders to avoid duplicates

    //GAME SETTINGS
    public static final int BOWL_HEIGHT = 110;
    public static final int BOWL_WIDTH = 85;
    public static final int BOWL_SPEED = 600;
    public static final int INGREDIENT_SIZE = 65;
    public static final float bgMusicVolume = 0.5f;
    public static final float soundVolume = 0.8f;

    //UI SCALING CONSTANTS
    public static final float HEART_SIZE = 50f;
    public static final float QUESTION_MARK_SIZE = 40f;
    public static final float BUBBLE_WIDTH = 120f;
    public static final float BUBBLE_HEIGHT = 102f;
    public static final float CUSTOMER_SIZE = 150f;
    public static final float ORDER_SIZE = 80f; // Smaller for multiple orders

    //GAME OBJECTS
    Bowl bowl;
    ArrayList<Ingredient> ingredients;
    Random random;
    float spawnTimer;
    float spawnInterval = 1.0f;
    String lastSpawnedType = "";

    //CSV DATA
    static ArrayList<Recipe> recipesArrayList = YourGameScreen.readCSVRecipes("src/main/resources/recipes.csv");
    ArrayList<String> ingredientTypes = readCSVIngredients("src/main/resources/ingredients.csv");

    int countIngredientsCaught;

    static int lives = 3; // Lives persist across waves

    //POPUP -- Memorize Recipe
    boolean memorizeMode = true;
    float memorizeTime = 5f;

    // CUSTOMER SYSTEM
    private int currentCustomer = 0;
    private int previousCustomer = -1;
    private int totalCustomers = 8;

    // INFINITE MODE STATS
    private int highestWave = 1;

    public InfiniteGameScreen() {
        super(800, 800);
    }

    @Override
    public void show() {
        setWave(currentWave);
        caughtIngredients.clear(); // CHANGED: Clear the caught ingredients list

        //Pop-up setup
        memorizeMode = true;
        memorizeTime = 5f;
        GameApp.addTexture("popup_gui", "textures/infPopup.png");

        //BOWL
        bowl = new Bowl();
        bowl.x = getWorldWidth() / 2 - (float) BOWL_HEIGHT / 2;
        bowl.y = 15;

        //INGREDIENT SYSTEM
        ingredients = new ArrayList<>();
        random = new Random();
        spawnTimer = 0;
        countIngredientsCaught = 0;

        // TEXTURES
        GameApp.addTexture("bowl", "textures/bowl.png");
        GameApp.addTexture("life", "textures/life.png");
        GameApp.addTexture("background", "textures/playingbg.png");
        GameApp.addTexture("speech_bubble", "textures/text_bubble.png");
        GameApp.addTexture("circle_bubble", "textures/circle_bubble.png");
        GameApp.addTexture("infbubble", "textures/infFrame.png");

        // Hearts textures
        GameApp.addTexture("heart_full", "textures/heart_full.png");
        GameApp.addTexture("heart_empty", "textures/heart_empty.png");

        // Questionmark textures
        GameApp.addTexture("question_mark", "textures/questionmark.png");

        // Load all customer textures
        for (int i = 0; i < totalCustomers; i++) {
            GameApp.addTexture("customer_" + i, "textures/customers/customer_" + i + ".png");
        }

        selectRandomCustomer();

        // Fonts
        GameApp.addFont("roboto", "fonts/Roboto_SemiBold.ttf", 16);
        GameApp.addFont("bubble", "fonts/bubble.ttf", 28);
        GameApp.addFont("bubble_small", "fonts/bubble.ttf", 20);
        GameApp.addFont("bubble_lists", "fonts/bubble.ttf", 12);
        GameApp.addFont("bubble_count", "fonts/bubble.ttf", 10);
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
            GameApp.switchScreen("MainMenuScreen");
        }

        if (lives <= 0) {
            InfiniteGameOverScreen.setWaveReached(currentWave);
            GameApp.switchScreen("InfiniteGameOverScreen");
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

        // Secret level skip - press 'S' key
        if (GameApp.isKeyJustPressed(Input.Keys.S)) {
            skipToNextWave();
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

        // Draw all top UI elements
        drawTopUI();

        GameApp.endSpriteRendering();
    }

    private void skipToNextWave() {
        ordersCompleted++;

        // CHANGED: Use caughtIngredients instead of caughtIngredientCounts.keySet()
        InfiniteLevelCompleteScreen.setCompletedRecipe(currentOrders.get(0),
                caughtIngredients, currentWave);

        currentWave++;
        if (currentWave > highestWave) {
            highestWave = currentWave;
        }

        selectRandomCustomer();
        GameApp.switchScreen("InfiniteLevelCompleteScreen");
    }

    private void drawTopUI() {
        drawCustomerWithOrders();
        drawLivesHearts(lives);
        drawProgress(); // CHANGED: No parameter needed
    }

    private void drawCustomerWithOrders() {
        float customerX = 0;
        float customerY = getWorldHeight() - CUSTOMER_SIZE;

        String customerTexture = "customer_" + currentCustomer;
        GameApp.drawTexture(customerTexture, customerX, customerY, CUSTOMER_SIZE, CUSTOMER_SIZE);

        // Draw multiple orders horizontally
        float bubbleStartX = customerX + CUSTOMER_SIZE;
        float bubbleY = getWorldHeight() - BUBBLE_HEIGHT - 35;
        float bubbleSpacing = 10;

        for (int i = 0; i < currentOrders.size(); i++) {
            float bubbleX = bubbleStartX + (i * (BUBBLE_WIDTH + bubbleSpacing));
            float orderX = (bubbleX + (BUBBLE_HEIGHT - ORDER_SIZE) / 2);
            float orderY = bubbleY + (BUBBLE_HEIGHT - ORDER_SIZE) / 2;

            GameApp.drawTexture("infbubble", bubbleX, bubbleY, BUBBLE_HEIGHT, BUBBLE_HEIGHT);
            GameApp.drawTexture(currentOrders.get(i).name, orderX, orderY, ORDER_SIZE, ORDER_SIZE);
        }
    }

    private void drawLivesHearts(int lives) {
        int maxLives = 3;
        float spacing = 10;

        float totalWidth = (HEART_SIZE * maxLives) + (spacing * (maxLives - 1));
        float startX = getWorldWidth() - totalWidth - 20;
        float heartY = getWorldHeight() - HEART_SIZE - 20;

        for (int i = 0; i < maxLives; i++) {
            float heartX = startX + (i * (HEART_SIZE + spacing));

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

        GameApp.drawTexture("popup_gui", popupX, popupY, popupW, popupH);

        // Draw the two orders in the CENTER
        float recipeSize = 150;
        float recipeSpacing = 200;
        float totalRecipeWidth = (recipeSize * currentOrders.size()) + (recipeSpacing * (currentOrders.size() - 1));
        float recipeStartX = (popupW - totalRecipeWidth) / 2;
        float recipeY = popupH / 2 + 50; // Center vertically

        for (int i = 0; i < currentOrders.size(); i++) {
            float recipeX = recipeStartX + (i * (recipeSize + recipeSpacing));

            float bgPadding = 10;
            float bgSize = recipeSize + (bgPadding * 2);
            GameApp.drawTexture("infbubble", recipeX - bgPadding, recipeY + 100 - bgPadding, bgSize, bgSize);

            GameApp.drawTexture(currentOrders.get(i).name, recipeX, recipeY + 100, recipeSize, recipeSize);
        }

        // Timer and text ABOVE the orders
        float counterBubbleX = (popupW - 100) / 2; // Center the bubble
        float counterBubbleY = recipeY + recipeSize + 40; // Above the orders

        GameApp.drawTexture("circle_bubble", counterBubbleX, counterBubbleY - 55, 100, 88);

// Center the timer text inside the bubble
        String timerText = String.valueOf((int) memorizeTime);
        float textWidthM = GameApp.getTextWidth("bubble", "3");
        float textHeightM = GameApp.getTextHeight("bubble", "3");

// Calculate centered position
        float centeredX = counterBubbleX + (100 - textWidthM) / 2;
        float centeredY = counterBubbleY - 55 + (88 - textHeightM) / 2;

        GameApp.drawText("bubble", timerText, centeredX, centeredY, "customLine");

        float textWidth = GameApp.getTextWidth("bubble","Memorize this recipe!");

        GameApp.drawText("bubble", "Memorize this recipe!", (getWorldWidth() - textWidth) / 2, popupH - 50, "customLine");

        // Draw combined ingredient list BELOW the orders
        float listStartX = 80;
        float listY = 455;
        float ingredientSize = 60;
        float rowHeight = 75;
        float columnSpacing = 400;

        float currentX = listStartX;
        float currentY = listY;

        int ingredientCounter = 0;
        int itemsPerColumn = 6;

        for (Map.Entry<String, Integer> entry : ingredientCounts.entrySet()) {
            if (ingredientCounter == itemsPerColumn) {
                currentX += columnSpacing;
                currentY = listY;
            }

            String ingredientName = entry.getKey();
            int count = entry.getValue();

            GameApp.drawTexture(ingredientName, currentX - 15, currentY - 20, ingredientSize, ingredientSize);

            String ingredientText = ingredientName.replace("_", " ");
            GameApp.drawText("bubble_lists", "x" + count + " " + ingredientText, currentX + ingredientSize - 15,
                    currentY , "customLine");

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

    private void spawnIngredients(float delta) {
        spawnTimer += delta;

        // Speed increases based on orders completed (every order makes it slightly faster)
        int speedBoost = ordersCompleted * 15; // +15 speed per order
        int ingredientSpeed = 200 + speedBoost;
        ingredientSpeed = Math.min(600, ingredientSpeed);

        // Spawn interval decreases with orders completed
        float adjustedSpawnInterval = spawnInterval - (ordersCompleted * 0.015f);
        if (adjustedSpawnInterval < 0.3f) adjustedSpawnInterval = 0.3f;

        if (spawnTimer >= adjustedSpawnInterval) {
            int minIngredientsPerSpawn = 1;
            int maxIngredientsPerSpawn = 2;

            if (ordersCompleted > 10) {
                minIngredientsPerSpawn = 2;
                maxIngredientsPerSpawn = 4;
            }
            if (ordersCompleted > 20) {
                maxIngredientsPerSpawn = 5;
            }
            if (ordersCompleted > 30) {
                maxIngredientsPerSpawn = 6;
            }

            int numToSpawn = random.nextInt(maxIngredientsPerSpawn - minIngredientsPerSpawn + 1)
                    + minIngredientsPerSpawn;

            for (int i = 0; i < numToSpawn; i++) {
                spawnSingleIngredient(ingredientSpeed);
            }

            spawnTimer = 0;
        }
    }

    private void spawnSingleIngredient(int baseSpeed) {
        String randomType;

        if (random.nextFloat() < 0.6f && !neededIngredients.isEmpty()) {
            ArrayList<String> stillNeeded = new ArrayList<>(neededIngredients);
            if (!stillNeeded.isEmpty()) {
                randomType = stillNeeded.get(random.nextInt(stillNeeded.size()));
            } else {
                randomType = ingredientTypes.get(SaxionApp.getRandomValueBetween(0, ingredientTypes.size()));
            }
        } else {
            randomType = ingredientTypes.get(SaxionApp.getRandomValueBetween(0, ingredientTypes.size()));
        }

        if (lastSpawnedType != null) {
            while (randomType.equals(lastSpawnedType)) {
                randomType = ingredientTypes.get(SaxionApp.getRandomValueBetween(0, ingredientTypes.size()));
            }
            lastSpawnedType = randomType;
        }

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

        newIngredient.y = getWorldHeight() - 200;
        ingredients.add(newIngredient);
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

                // Check if it's a bomb first (deadly!)
                if (ingredient.type.equals("bomb")) {
                    AudioControl.playSound("bad caught", soundVolume);
                    lives = 0;
                    ingredients.remove(i);
                    break;
                }

                if (neededIngredients.contains(ingredient.type)) {
                    neededIngredients.remove(ingredient.type);
                    caughtIngredients.add(ingredient.type);
                    AudioControl.playSound("correct caught", soundVolume);

                    if (neededIngredients.isEmpty()) {
                        ordersCompleted++;
                        InfiniteLevelCompleteScreen.setCompletedRecipe(currentOrders.get(0),
                                caughtIngredients, currentWave);

                        currentWave++;
                        if (currentWave > highestWave) {
                            highestWave = currentWave;
                        }

                        selectRandomCustomer();
                        GameApp.switchScreen("InfiniteLevelCompleteScreen");
                        return;
                    }
                } else {
                    if (ingredient.type.equals("life") && lives < 3) {
                        lives++;
                    } else if (ingredient.type.contains("rotten")) {
                        AudioControl.playSound("bad caught", soundVolume);
                        lives = lives - 2;
                    } else if (!ingredient.type.equals("life")) {
                        AudioControl.playSound("bad caught", soundVolume);
                        lives--;
                    }
                }

                ingredients.remove(i);
                break;
            }
        }
    }

    private boolean isColliding(Ingredient ingredient, Bowl bowl) {
        return ingredient.x < bowl.x + BOWL_WIDTH &&
                ingredient.x + INGREDIENT_SIZE > bowl.x &&
                ingredient.y < bowl.y + BOWL_WIDTH &&
                ingredient.y + INGREDIENT_SIZE > bowl.y;
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

    public static void resetGame() {
        currentWave = 1;
        ordersCompleted = 0;
        lives = 3;
        recentOrderIndices.clear();
        setWave(1);
    }

    public static void setWave(int wave) {
        currentWave = wave;

        // Always 2 orders
        int numRecipes = 2;

        currentOrders = new ArrayList<>();
        ingredientCounts = new HashMap<>();
        neededIngredients = new ArrayList<>();

        // Select 2 random different recipes
        Random rand = new Random();
        ArrayList<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < recipesArrayList.size(); i++) {
            // Avoid recently used orders
            if (!recentOrderIndices.contains(i)) {
                availableIndices.add(i);
            }
        }

        // If we've used all recipes, clear history except last 3
        if (availableIndices.size() < 2) {
            if (recentOrderIndices.size() > 3) {
                recentOrderIndices.remove(0);
            }
            availableIndices.clear();
            for (int i = 0; i < recipesArrayList.size(); i++) {
                if (!recentOrderIndices.contains(i)) {
                    availableIndices.add(i);
                }
            }
        }

        // Select 2 different recipes
        for (int i = 0; i < numRecipes && !availableIndices.isEmpty(); i++) {
            int randomIndex = rand.nextInt(availableIndices.size());
            int recipeIndex = availableIndices.remove(randomIndex);

            Recipe selectedRecipe = recipesArrayList.get(recipeIndex);
            currentOrders.add(selectedRecipe);
            recentOrderIndices.add(recipeIndex);

            // Combine ingredients
            for (String ingredient : selectedRecipe.recipeIngredientList) {
                ingredientCounts.put(ingredient, ingredientCounts.getOrDefault(ingredient, 0) + 1);
                neededIngredients.add(ingredient);
            }
        }

        // Keep only last 5 orders in history
        while (recentOrderIndices.size() > 5) {
            recentOrderIndices.remove(0);
        }

        // Don't reset lives
        System.out.println("Wave " + wave + ": " + currentOrders.size() + " orders");
    }

    private void drawProgress() {
        float spacing = 10;
        float startX = getWorldWidth() - QUESTION_MARK_SIZE - 20;
        float baseY = getWorldHeight() - QUESTION_MARK_SIZE * 3;
        int itemsPerRow = 6;

        int totalIngredients = caughtIngredients.size() + neededIngredients.size();

        for (int i = 0; i < totalIngredients; i++) {
            // Calculate position with multi-line support
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            float progressX = startX - (col * (QUESTION_MARK_SIZE + spacing));
            float progressY = baseY - (row * (QUESTION_MARK_SIZE + spacing + 10));

            // Draw caught ingredients first, then question marks for remaining
            if (i < caughtIngredients.size()) {
                GameApp.drawTexture(caughtIngredients.get(i), progressX, progressY,
                        QUESTION_MARK_SIZE, QUESTION_MARK_SIZE * 0.85f);
            } else {
                GameApp.drawTexture("question_mark", progressX, progressY,
                        QUESTION_MARK_SIZE, QUESTION_MARK_SIZE * 0.85f);
            }
        }
    }
}