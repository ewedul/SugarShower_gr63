package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InfiniteLevelCompleteScreen extends ScalableGameScreen {

    private static ArrayList<Recipe> completedOrders;
    private static ArrayList<String> ingredientsCaught;
    private static int currentWave;
    public static final float soundVolume = 0.8f;

    private HashMap<String, Integer> ingredientCounts;

    public InfiniteLevelCompleteScreen() {
        super(800, 800);
    }

    public static void setCompletedRecipe(Recipe recipe, ArrayList<String> caught, int wave) {
        // Get the current orders from InfiniteGameScreen (should be 2 recipes)
        completedOrders = InfiniteGameScreen.currentOrders;
        ingredientsCaught = new ArrayList<>(caught);
        currentWave = wave;
    }

    @Override
    public void show() {
        GameApp.addTexture("levelcomplete_bg", "textures/playingbg.png");
        GameApp.addTexture("popup_gui", "textures/infPopup.png");
        GameApp.addTexture("infbubble", "textures/infFrame.png");
        GameApp.addTexture("circle_bubble", "textures/circle_bubble.png");
        GameApp.addColor("customLine", 64, 15, 38);

        GameApp.addTexture("next_button_normal", "textures/buttons/button2.png");
        GameApp.addTexture("next_button_selected", "textures/buttons/button2.png");

        GameApp.addFont("roboto", "fonts/Roboto_SemiBold.ttf", 16);
        GameApp.addFont("bubble", "fonts/bubble.ttf", 28);
        GameApp.addFont("bubble_small", "fonts/bubble.ttf", 20);
        GameApp.addFont("bubble_lists", "fonts/bubble.ttf", 12);
        GameApp.addFont("bubble_count", "fonts/bubble.ttf", 10);

        GameApp.addSound("correct caught", "audio/correct caught.ogg");

        // Load all finished product textures for the orders
        for (Recipe recipe : completedOrders) {
            String finishedProductPath = getFinishedProductPath(recipe.name);
            GameApp.addTexture(recipe.name, finishedProductPath);
        }

        // Load all ingredient textures
        for (Recipe recipe : completedOrders) {
            for (String ingredient : recipe.recipeIngredientList) {
                String ingredientPath = getIngredientTexturePath(ingredient);
                GameApp.addTexture(ingredient, ingredientPath);
            }
        }

        calculateIngredientCounts();
    }

    private void calculateIngredientCounts() {
        ingredientCounts = new HashMap<>();

        // Combine ingredients from both orders
        for (Recipe recipe : completedOrders) {
            for (String ingredient : recipe.recipeIngredientList) {
                ingredientCounts.put(ingredient,
                        ingredientCounts.getOrDefault(ingredient, 0) + 1);
            }
        }

        AudioControl.playSound("correct caught", soundVolume);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        float buttonWidth = 150;
        float buttonHeight = 60;
        float buttonX = getWorldWidth() - buttonWidth - 50;
        float buttonY = 100;

        if (GameApp.pointInRect(getMouseX(), getMouseY(), buttonX, buttonY, buttonWidth, buttonHeight)
                && GameApp.isButtonJustPressed(Input.Buttons.LEFT)) {
            GameApp.switchScreen("InfiniteGameScreen");
        }

        if (GameApp.isKeyJustPressed(Input.Keys.ENTER) ||
                GameApp.isKeyJustPressed(Input.Keys.SPACE)) {
            GameApp.switchScreen("InfiniteGameScreen");
        }

        if (GameApp.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GameApp.switchScreen("MainMenuScreen");
        }

        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        // Draw the background first
        GameApp.drawTexture("levelcomplete_bg", 0, 0, getWorldWidth(), getWorldHeight());

        // Draw the popup background on top
        float popupW = getWorldWidth();
        float popupH = getWorldHeight();
        float popupX = getWorldWidth() / 2 - popupW / 2;
        float popupY = getWorldHeight() / 2 - popupH / 2;

        GameApp.drawTexture("popup_gui", popupX, popupY, popupW, popupH);

        // Draw the two finished products in the CENTER (matching memorize layout)
        float recipeSize = 150;
        float recipeSpacing = 200;
        float totalRecipeWidth = (recipeSize * completedOrders.size()) + (recipeSpacing * (completedOrders.size() - 1));
        float recipeStartX = (popupW - totalRecipeWidth) / 2;
        float recipeY = popupH / 2 + 50; // Center vertically

        for (int i = 0; i < completedOrders.size(); i++) {
            float recipeX = recipeStartX + (i * (recipeSize + recipeSpacing));

            float bgPadding = 10;
            float bgSize = recipeSize + (bgPadding * 2);
            GameApp.drawTexture("infbubble", recipeX - bgPadding, recipeY + 100 - bgPadding, bgSize, bgSize);

            GameApp.drawTexture(completedOrders.get(i).name, recipeX, recipeY + 100, recipeSize, recipeSize);
        }

        float textwidth1 = GameApp.getTextWidth("bubble","Recipe Complete!");
        float textWidth2 = GameApp.getTextWidth("bubble_count","Press ENTER to continue");

        // "Recipe Complete!" text above the circle bubble
        GameApp.drawText("bubble", "Recipe Complete!", (getWorldWidth() - textwidth1) / 2, getWorldHeight() - 50, "customLine");
        GameApp.drawText("bubble_count","Press ENTER to continue", (getWorldWidth() - textWidth2) /2, getWorldHeight() - 70, "customLine");

        // Draw combined ingredient list BELOW the product (matching InfiniteGameScreen layout)
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

        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {
        GameApp.stopAllAudio();

        GameApp.disposeTexture("levelcomplete_bg");
        GameApp.disposeTexture("popup_gui");
        GameApp.disposeTexture("infbubble");
        GameApp.disposeTexture("circle_bubble");
        GameApp.disposeTexture("next_button_normal");
        GameApp.disposeTexture("next_button_selected");
        GameApp.disposeSound("correct caught");

        GameApp.disposeFont("bubble");
        GameApp.disposeFont("bubble_small");
        GameApp.disposeFont("roboto");
        GameApp.disposeFont("bubble_lists");
        GameApp.disposeFont("bubble_count");

        // Dispose all recipe textures
        for (Recipe recipe : completedOrders) {
            GameApp.disposeTexture(recipe.name);
        }

        // Dispose all ingredient textures
        for (Recipe recipe : completedOrders) {
            for (String ingredient : recipe.recipeIngredientList) {
                GameApp.disposeTexture(ingredient);
            }
        }
    }

    private String getFinishedProductPath(String recipeName) {
        return "textures/finished_products/" + recipeName + ".png";
    }

    private String getIngredientTexturePath(String ingredientName) {
        return "textures/ingredients/" + ingredientName + ".png";
    }
}