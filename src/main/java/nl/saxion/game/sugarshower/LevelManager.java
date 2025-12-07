package nl.saxion.game.sugarshower;

import nl.saxion.app.CsvReader;

import java.util.ArrayList;

public class LevelManager {
    int currentLevelIndex;
    int currentPhase = 1;
    ArrayList<Recipe> allRecipes = new ArrayList<>();


    public void loadAllRecipesToManager(String filename) {
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

            allRecipes.add(newRecipe);
        }
    }

    public ArrayList<String> getCurrentPhaseIngredients() {
        ArrayList<String>currentPhaseIngredientTypes = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (recipe.phase == currentPhase) {
                for (String ing : recipe.recipeIngredientList) {
                    if (!currentPhaseIngredientTypes.contains(ing)) {
                        currentPhaseIngredientTypes.add(ing);
                    }
                }
            }
        }
        return currentPhaseIngredientTypes;
    }


    public ArrayList<String> getCurrentLevelRequiredIngredients() {
        ArrayList<String> requiredIngredients = new ArrayList<>();
        for(Recipe r: allRecipes){
            if(r.level==currentLevelIndex){
               requiredIngredients = r.recipeIngredientList;
            }
        }
        return requiredIngredients;

    }
}




