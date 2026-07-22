package com.juzi.nhaddtingsjuzi.recipe;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.juzi.nhaddtingsjuzi.registry.ModItems;

import appeng.api.AEApi;
import gregtech.api.enums.GTValues;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

/** MV assembler recipe for the combined item/fluid terminal. */
public final class RecipeDualTerminal {

    private RecipeDualTerminal() {}

    public static void register() {
        GTRecipeBuilder.builder()
                .itemInputs(
                        require("AE2 crafting terminal", AEApi.instance()
                                .definitions()
                                .parts()
                                .craftingTerminal()
                                .maybeStack(1)
                                .orNull()),
                        require("AE2FC fluid terminal", ItemAndBlockHolder.FLUID_TERM == null
                                ? null
                                : ItemAndBlockHolder.FLUID_TERM.stack()),
                        require("AE2 logic processors", AEApi.instance()
                                .definitions()
                                .materials()
                                .logicProcessor()
                                .maybeStack(4)
                                .orNull()))
                .itemOutputs(require("dual terminal", new ItemStack(ModItems.dualTerminal)))
                .duration(10 * GTRecipeBuilder.SECONDS)
                .eut(GTValues.V[2])
                .addTo(RecipeMaps.assemblerRecipes);
    }

    private static ItemStack require(String ingredient, ItemStack stack) {
        if (GTUtility.isStackInvalid(stack)) {
            throw new IllegalStateException("Missing dual terminal ingredient: " + ingredient);
        }
        return stack;
    }
}
