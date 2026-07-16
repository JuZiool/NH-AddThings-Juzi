package com.juzi.nhaddtingsjuzi.recipe;

import com.juzi.nhaddtingsjuzi.item.ItemTieredVajra;

import ic2.api.item.ElectricItem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedVajraRecipe extends ShapedOreRecipe {

    private static final int BATTERY_SLOT = 7;

    private final ItemTieredVajra resultItem;

    public ShapedVajraRecipe(ItemTieredVajra resultItem, Object... recipe) {
        super(new ItemStack(resultItem), recipe);
        this.resultItem = resultItem;
        setMirrored(false);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack battery = inventory.getStackInSlot(BATTERY_SLOT);
        double charge = battery == null ? 0.0D : ElectricItem.manager.getCharge(battery);
        return resultItem.createStackWithCharge(charge);
    }
}
