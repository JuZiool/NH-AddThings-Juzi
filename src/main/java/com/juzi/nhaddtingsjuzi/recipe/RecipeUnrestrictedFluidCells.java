package com.juzi.nhaddtingsjuzi.recipe;

import com.juzi.nhaddtingsjuzi.registry.ModItems;
import com.juzi.nhaddtingsjuzi.storage.UnrestrictedFluidCellComponents;
import com.juzi.nhaddtingsjuzi.storage.UnrestrictedFluidCellItem;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public final class RecipeUnrestrictedFluidCells {

    private RecipeUnrestrictedFluidCells() {}

    public static void register() {
        addRecipeIfAvailable(ModItems.fluidCell1k, UnrestrictedFluidCellComponents.forCapacity(1024));
        addRecipeIfAvailable(ModItems.fluidCell4k, UnrestrictedFluidCellComponents.forCapacity(4096));
        addRecipeIfAvailable(ModItems.fluidCell16k, UnrestrictedFluidCellComponents.forCapacity(16384));
        addRecipeIfAvailable(ModItems.fluidCell64k, UnrestrictedFluidCellComponents.forCapacity(65536));
    }

    private static void addRecipeIfAvailable(UnrestrictedFluidCellItem outputItem, ItemStack component) {
        if (component != null && component.getItem() != null) addRecipe(outputItem, component);
    }

    private static void addRecipe(UnrestrictedFluidCellItem outputItem, ItemStack component) {
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(outputItem),
                "SC",
                'S', new ItemStack(ModItems.unrestrictedShell),
                'C', component));
    }
}
