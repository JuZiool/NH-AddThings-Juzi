package com.juzi.nhaddtingsjuzi.compat;

import java.util.ArrayList;
import java.util.List;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public final class PlayerElectricInventory {

    private PlayerElectricInventory() {}

    public static List<ItemStack> collect(EntityPlayer player) {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        addAll(stacks, player.inventory.mainInventory);
        addAll(stacks, player.inventory.armorInventory);
        try {
            IInventory baubles = BaublesApi.getBaubles(player);
            if (baubles != null) {
                for (int slot = 0; slot < baubles.getSizeInventory(); slot++) {
                    add(stacks, baubles.getStackInSlot(slot));
                }
            }
        } catch (Throwable ignored) {
        }
        return stacks;
    }

    private static void addAll(List<ItemStack> stacks, ItemStack[] inventory) {
        for (ItemStack stack : inventory) {
            add(stacks, stack);
        }
    }

    private static void add(List<ItemStack> stacks, ItemStack stack) {
        if (stack != null && stack.stackSize > 0) {
            stacks.add(stack);
        }
    }
}
