package com.juzi.nhaddtingsjuzi.recipe;

import com.juzi.nhaddtingsjuzi.registry.ModMachines;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

public final class RecipeChargingStation {

    private RecipeChargingStation() {}

    public static void register() {
        GTRecipeBuilder.builder()
                .itemInputs(
                        require("EV machine hull", ItemList.Hull_EV.get(
                                ChargingStationRecipeSpec.EV_HULLS)),
                        require("IV circuit", GTOreDictUnificator.get(
                                OrePrefixes.circuit,
                                Materials.IV,
                                ChargingStationRecipeSpec.IV_CIRCUITS)),
                        require("HV emitter", ItemList.Emitter_HV.get(
                                ChargingStationRecipeSpec.HV_EMITTERS)),
                        require("HV sensor", ItemList.Sensor_HV.get(
                                ChargingStationRecipeSpec.HV_SENSORS)),
                        require("LV field generator", ItemList.Field_Generator_LV.get(
                                ChargingStationRecipeSpec.LV_FIELD_GENERATORS)))
                .itemOutputs(require("charging station", ModMachines.chargingStationStack))
                .duration(30 * GTRecipeBuilder.SECONDS)
                .eut(ChargingStationRecipeSpec.EUT)
                .addTo(RecipeMaps.assemblerRecipes);
    }

    private static ItemStack require(String ingredient, ItemStack stack) {
        if (GTUtility.isStackInvalid(stack)) {
            throw new IllegalStateException("Missing Charging Station ingredient: " + ingredient);
        }
        return stack;
    }
}
