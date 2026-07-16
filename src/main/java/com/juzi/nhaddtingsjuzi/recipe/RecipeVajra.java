package com.juzi.nhaddtingsjuzi.recipe;

import com.juzi.nhaddtingsjuzi.registry.ModItems;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

public final class RecipeVajra {

    private RecipeVajra() {}

    public static void register() {
        ItemStack doubleBlackSteelPlate = require(
                "double Black Steel plate",
                GTOreDictUnificator.get(OrePrefixes.plateDouble, Materials.BlackSteel, 1));
        ItemStack hvEmitter = require("HV emitter", ItemList.Emitter_HV.get(1));
        ItemStack carbonFiberPlate = require(
                "carbon-fiber plate", GTModHandler.getIC2Item("carbonPlate", 1));
        ItemStack lvFieldGenerator = require(
                "LV field generator", ItemList.Field_Generator_LV.get(1));
        ItemStack denseSteelPlate = require(
                "dense Steel plate",
                GTOreDictUnificator.get(OrePrefixes.plateDense, Materials.Steel, 1));
        ItemStack hvLithiumBattery = require(
                "HV lithium battery", ItemList.Battery_RE_HV_Lithium.get(1));

        GameRegistry.addRecipe(new ShapedVajraRecipe(
                ModItems.hvVajra,
                "PEP",
                "CFC",
                "RUR",
                'P', doubleBlackSteelPlate,
                'E', hvEmitter,
                'C', carbonFiberPlate,
                'F', lvFieldGenerator,
                'R', denseSteelPlate,
                'U', hvLithiumBattery));
    }

    private static ItemStack require(String ingredient, ItemStack stack) {
        if (GTUtility.isStackInvalid(stack)) {
            throw new IllegalStateException("Missing HV Vajra ingredient: " + ingredient);
        }
        return stack;
    }
}
