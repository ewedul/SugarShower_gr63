package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.app.CsvReader;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

import java.util.ArrayList;
import java.util.Random;

public class VictoryScreen extends ScalableGameScreen {

    private int selectedButton = 0;

    Random random;
    float spawnTimer;
    float spawnInterval = 1.0f;

    ArrayList<String> customerTypes;
    ArrayList<String> finishedProductsTypes;
    ArrayList<Ingredient> fallingProducts;
    String lastSpawnedProduct = "";

    //=======================VARIABLES FOR UI ELEMENTS=======================
    float worldWidth;
    float worldHeight;

    // Button dimensions
    float normalWidth = 254;
    float normalHeight = 100;
    float selectedWidth = 274;
    float selectedHeight = 120;

    // Button positions (centered horizontally)
    float centerX = getWorldWidth() / 2;
    float startButtonY = (getWorldHeight() - 300) / 2 + 46; // Higher position
    float exitButtonY = (getWorldHeight() - 300) / 2 - 46;  // Lower position


    //World coordinate of button (different due to drawTextureCentered method)
    float startX = centerX - normalWidth / 2;
    float startY = startButtonY - normalHeight / 2;
    float exitY = exitButtonY - normalHeight / 2;


    // ===== SPEECH BUBBLE =====
    // Speech bubble dimensions
    float speechWidth = 331;
    float speechHeight = 205;

    // Position speech bubble centered above the buttons
    float speechX = (centerX - speechWidth / 2) - 10;
    float speechY = startButtonY + 120; // Position above retry button
    float PRODUCT_SIZE = (float)1.5*YourGameScreen.INGREDIENT_SIZE;


    public VictoryScreen() {
        super(800, 800);
    }

    @Override
    public void show() {
        worldWidth = GameApp.getWorldWidth();
        worldHeight = GameApp.getWorldHeight();

        random = new Random();
        fallingProducts = new ArrayList<>();

        //Load name of finished products
        finishedProductsTypes = new ArrayList<>();
        CsvReader productsReader = new CsvReader("src/main/resources/recipes.csv");
        productsReader.setSeparator(',');
        productsReader.skipRow();
        while (productsReader.loadRow()) {
            String product;
            product = productsReader.getString(2);
            finishedProductsTypes.add(product);
            System.out.println("Add:" + product);
        }


        GameApp.addTexture("gameover_background", "textures/gameover_bg.png");

        // Add fonts for speech bubble text
        GameApp.addFont("bubble", "fonts/bubble.ttf", 20);
        GameApp.addFont("bubble_big", "fonts/bubble.ttf", 35);

        GameApp.addTexture("mainmenu", "textures/buttons/mainmenu_button.png");
        GameApp.addTexture("retry", "textures/buttons/retry_button.png");
        GameApp.addTexture("speech", "textures/speech_bubble.png");

        GameApp.addTexture("button", "textures/buttons/button2.png");
        GameApp.addColor("customcolor", 64, 15, 38);
        GameApp.addMusic("bg-music", "audio/The_Biggest_Smile.mp3");
        AudioControl.playMusic("bg-music", true, YourGameScreen.bgMusicVolume);

        // Load all finished product image using recipe name
        for (String productType : finishedProductsTypes) {
            String finishedProductPath = getFinishedProductPath(productType);
            GameApp.addTexture(productType, finishedProductPath);
        }

    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // ===== INPUT HANDLING =====
        // Navigate with UP/DOWN arrow keys
        if (GameApp.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedButton = (selectedButton + 1) % 2; // Cycle: 0 -> 1 -> 0
        } else if (GameApp.isKeyJustPressed(Input.Keys.UP)) {
            selectedButton = (selectedButton - 1 + 2) % 2; // Cycle: 1 -> 0 -> 1
        }

        // Confirm selection with ENTER
        if (GameApp.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedButton == 0) {
                // Retry button pressed - go back to game
                YourGameScreen.setLevel(YourGameScreen.getCurrentLevel());
                GameApp.switchScreen("YourGameScreen");
            } else {
                // Main menu button pressed
                GameApp.switchScreen("MainMenuScreen");
            }
        }

        //Press M to turn on Mute Mode.
        if (GameApp.isKeyJustPressed(Input.Keys.M)) {
            AudioControl.toggleMuteMode();
            GameApp.getMusic("bg-music").
                    setVolume(AudioControl.muteMode ? 0f : YourGameScreen.bgMusicVolume);
        }

        //Button changes size when hovering mouse over it. Click to choose.
        float mouseX = getMouseX();
        float mouseY = getMouseY();

        if (GameApp.pointInRect(mouseX, mouseY, startX, exitY, normalWidth, normalHeight)) {
            selectedButton = 1;
            if (GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameApp.switchScreen("MainMenuScreen");
            }
        }

        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnOneFinishedProduct();
            spawnTimer = 0;

        }
        updateFallingProducts(delta);

        // ===== RENDERING =====
        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        // Draw background (full screen)
        GameApp.drawTexture("gameover_background", 0, 0, getWorldWidth(), getWorldHeight());

        // Draw button (centered)
        if (selectedButton == 0) {

            // mainmenu button is NOT selected - use normal texture
            GameApp.drawTextureCentered("mainmenu",
                    centerX, exitButtonY,
                    normalWidth, normalHeight);

        } else if (selectedButton == 1) {

            // mainmenu button is SELECTED - use larger highlighted texture
            GameApp.drawTextureCentered("mainmenu",
                    centerX, exitButtonY,
                    selectedWidth, selectedHeight);
        }

        // gameover
        GameApp.drawTextHorizontallyCentered("bubble_big", "CONGRATULATIONS!", centerX, getWorldHeight() - 50, "customcolor");

        // ===== SPEECH BUBBLE =====
        // Speech bubble dimensions
        float speechWidth = 331;
        float speechHeight = 205;

        // Position speech bubble centered above the buttons
        float speechX = (centerX - speechWidth / 2) - 10;
        float speechY = startButtonY + 120; // Position above retry button

        // Draw speech bubble
        GameApp.drawTexture("speech", speechX, speechY, speechWidth, speechHeight);

        // Draw text inside speech bubble (centered)
        String message = "NICE WORK!";
        String message2 = " YOU BEAT";
        String message3 = "SUGAR SHOWERS!";

        // Calculate text position to center it in the speech bubble
        float textCenterX = centerX;
        float textCenterY = speechY + (speechHeight / 2) + 20; // Adjust Y to center in bubble

        // Draw text centered in speech bubble
        GameApp.drawTextHorizontallyCentered("bubble", message,
                textCenterX, textCenterY + 25, "customcolor");
        GameApp.drawTextHorizontallyCentered("bubble", message2,
                textCenterX, textCenterY, "customcolor"); // Second line below
        GameApp.drawTextHorizontallyCentered("bubble", message3,
                textCenterX, textCenterY - 25, "customcolor");

        for (Ingredient product : fallingProducts) {
                GameApp.drawTexture(product.type, product.x, product.y,
                        PRODUCT_SIZE, PRODUCT_SIZE);
        }
        GameApp.endSpriteRendering();
    }

//    private boolean collideWithGameUI(Ingredient fallingProduct) {
//        return GameApp.rectOverlap(fallingProduct.x,fallingProduct.y,PRODUCT_SIZE,PRODUCT_SIZE,
//                speechX, speechY, speechWidth, speechHeight)
//
//                || GameApp.rectOverlap(fallingProduct.x,fallingProduct.y,PRODUCT_SIZE,PRODUCT_SIZE,
//                centerX, exitButtonY, normalWidth, normalHeight);

//    } //The dissapearing motion is too jerky to be of use.

    private void spawnOneFinishedProduct() {
        String randomType;

        randomType = finishedProductsTypes.get(random.nextInt(finishedProductsTypes.size()));

        if (lastSpawnedProduct != null) {
            while (randomType.equals(lastSpawnedProduct)) {
                randomType = finishedProductsTypes.get(random.nextInt(finishedProductsTypes.size()));
            }
            lastSpawnedProduct = randomType;
        }

        Ingredient newProduct = new Ingredient(randomType, 200 + random.nextInt(100));
        newProduct.x = random.nextInt(0, (int) getWorldWidth() - ((int) PRODUCT_SIZE + 100));
        newProduct.y = getWorldHeight();
        fallingProducts.add(newProduct);
        System.out.println("Spawn: " + newProduct.type);
    }

    private void updateFallingProducts(float delta) {
        for (int i = fallingProducts.size() - 1; i >= 0; i--) {
            Ingredient product = fallingProducts.get(i);

            if (product.active) {
                product.y -= product.speed * delta;

                if (product.y < -PRODUCT_SIZE) {
                    fallingProducts.remove(i);
                }
            }
        }
    }


    @Override
    public void hide() {
        GameApp.stopMusic("bg-music");
        GameApp.disposeTexture("gameover_background");
        GameApp.disposeTexture("button");
        //GameApp.disposeTexture("retry");
        GameApp.disposeTexture("mainmenu");
        GameApp.disposeTexture("speech");
        GameApp.disposeFont("bubble");
        GameApp.disposeFont("bubble_speech");
        GameApp.disposeMusic("bg-music");

        for (String productType : finishedProductsTypes) {
            GameApp.disposeTexture(productType);
        }

    }

    private String getFinishedProductPath(String recipeName) {
        return "textures/finished_products/" + recipeName + ".png";
    }
}