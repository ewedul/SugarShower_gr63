package nl.saxion.game.sugarshower;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;
import nl.saxion.app.CsvReader;
import java.util.ArrayList;
import java.util.HashMap;

public class LevelCompleteScreen extends ScalableGameScreen {

    // Recipe information passed from game screen
    private static Recipe completedRecipe;
    private static ArrayList<String> ingredientsCaught;

    // Track ingredient counts (how many times each was caught)
    private HashMap<String, Integer> ingredientCounts;

    public LevelCompleteScreen() {
        super(800, 800);
    }

    /**
     * Call this method from YourGameScreen when level is complete
     * to pass the recipe data to this screen
     */
    public static void setCompletedRecipe(Recipe recipe, ArrayList<String> caught) {
        completedRecipe = recipe;
        ingredientsCaught = new ArrayList<>(caught);
    }

    @Override
    public void show() {
        // Load CUSTOM background for level complete screen
        GameApp.addTexture("levelcomplete_bg", "textures/levelcomplete_bg.png");

        // Load custom color
        GameApp.addColor("customLine", 64, 15, 38);

        // Load button textures
        GameApp.addTexture("next_button_normal", "textures/buttons/button2.png");
        GameApp.addTexture("next_button_selected", "textures/buttons/button2.png");

        // Load fonts - INCLUDING BUBBLE FONT
        GameApp.addFont("bubble", "fonts/bubble.ttf", 40);      // Big bubble font for title
        GameApp.addFont("bubble_small", "fonts/bubble.ttf", 25); // Smaller bubble for subtitle
        GameApp.addFont("bubble_lists", "fonts/bubble.ttf", 15); // for ing name
        GameApp.addFont("bubble_count", "fonts/bubble.ttf", 12); // for count
        GameApp.addFont("roboto", "fonts/Roboto_SemiBold.ttf", 20); // For ingredient list

        // Load the finished product image using recipe name
        String finishedProductPath = getFinishedProductPath(completedRecipe.name);
        GameApp.addTexture("finished_product", finishedProductPath);

        // Load ingredient textures (small versions for the list)
        for (String ingredient : completedRecipe.recipeIngredientList) {
            String ingredientPath = getIngredientTexturePath(ingredient);
            GameApp.addTexture(ingredient, ingredientPath);
        }

        // Count how many times each ingredient was caught
        calculateIngredientCounts();
    }

    /**
     * Calculate how many times each ingredient was caught
     * to display in the format "2/2" or "1/1"
     */
    private void calculateIngredientCounts() {
        ingredientCounts = new HashMap<>();

        // Count total needed for each ingredient type
        for (String ingredient : completedRecipe.recipeIngredientList) {
            ingredientCounts.put(ingredient,
                    ingredientCounts.getOrDefault(ingredient, 0) + 1);
        }

        System.out.println("Ingredient counts: " + ingredientCounts);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // Press ENTER or SPACE to continue to next level
        if (GameApp.isKeyJustPressed(Input.Keys.ENTER) ||
                GameApp.isKeyJustPressed(Input.Keys.SPACE)) {
            // Go back to game screen for next level
            GameApp.switchScreen("YourGameScreen");
        }

        // Press ESC to go to main menu
        if (GameApp.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GameApp.switchScreen("MainMenuScreen");
        }

        // ===== RENDERING =====
        GameApp.clearScreen();
        GameApp.startSpriteRendering();

        // Draw CUSTOM background (full screen)
        GameApp.drawTexture("levelcomplete_bg", 0, 0, getWorldWidth(), getWorldHeight());

        // Draw subtitle with smaller bubble font
        GameApp.drawText("bubble_small", completedRecipe.name.replace("_", " "),
                80, 550, "customLine");

        // ===== LAYOUT =====
        // Left side: Big finished product picture
        float bigPicX = 81;
        float bigPicY = 285;

        // Right side: Ingredient list
        float listX = 450;
        float listY = getWorldHeight() - 305;
        float ingredientSize = 60;
        float rowHeight = 70;

        // Draw big finished product picture
        GameApp.drawTexture("finished_product", bigPicX, bigPicY, 254, 232);

        // Draw ingredient list on the right side
        float currentY = listY;

        // Use a set to track already displayed ingredients (avoid duplicates in display)
        ArrayList<String> displayedIngredients = new ArrayList<>();

        for (String ingredient : completedRecipe.recipeIngredientList) {
            // Skip if already displayed (for ingredients that appear multiple times)
            if (displayedIngredients.contains(ingredient)) {
                continue;
            }
            displayedIngredients.add(ingredient);

            // Get count for this ingredient
            int count = ingredientCounts.get(ingredient);

            // Draw ingredient icon (small)
            GameApp.drawTexture(ingredient, listX, currentY, ingredientSize, ingredientSize);

            // Draw ingredient name and count
            String ingredientText = ingredient.replace("_", " ");
            GameApp.drawText("bubble_lists", ingredientText, listX + ingredientSize,
                    currentY + ingredientSize - 20, "customLine");

            // Draw count in format "2/2" or "1/1"
            String countText = count + "/" + count; // Always show as complete (X/X)
            GameApp.drawText("bubble_count", countText, listX + ingredientSize,
                    currentY + 10, "customLine");


            currentY -= rowHeight; // Move down for next ingredient
        }

        // Draw "NEXT" button at bottom right
        float buttonWidth = 150;
        float buttonHeight = 60;
        float buttonX = getWorldWidth() - buttonWidth - 50;
        float buttonY = 100;

        // Draw button (always show selected since it's the only button)
        GameApp.drawTexture("next_button_selected", buttonX, buttonY,
                buttonWidth, buttonHeight);


        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {
        // Clean up textures
        GameApp.disposeTexture("levelcomplete_bg"); // Dispose custom background
        GameApp.disposeTexture("next_button_normal");
        GameApp.disposeTexture("next_button_selected");
        GameApp.disposeTexture("finished_product");

        // Clean up fonts
        GameApp.disposeFont("bubble");
        GameApp.disposeFont("bubble_small");
        GameApp.disposeFont("roboto");

        // Clean up ingredient textures
        for (String ingredient : completedRecipe.recipeIngredientList) {
            GameApp.disposeTexture(ingredient);
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Get the texture path for finished product image
     * Uses the recipe name directly from CSV
     */
    private String getFinishedProductPath(String recipeName) {
        return "textures/finished_products/" + recipeName + ".png";
    }

    /**
     * Get the texture path for ingredient
     */
    private String getIngredientTexturePath(String ingredientName) {
        return "textures/ingredients/" + ingredientName + ".png";
    }
}