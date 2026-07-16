package com.juzi.nhaddtingsjuzi.registry;

import com.juzi.nhaddtingsjuzi.recipe.RecipeArcane;
import com.juzi.nhaddtingsjuzi.recipe.RecipeChargingStation;
import com.juzi.nhaddtingsjuzi.recipe.RecipeVajra;

public final class ModRecipes {

    private ModRecipes() {}

    public static void register() {
        RecipeArcane.register();
        RecipeVajra.register();
        RecipeChargingStation.register();
    }
}
