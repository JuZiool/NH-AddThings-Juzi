package com.juzi.nhaddtingsjuzi.compat;

import java.util.ArrayList;
import java.util.List;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public final class PlayerElectricInventory {

    private PlayerElectricInventory() {}

    public static List<PlayerItem> collect(EntityPlayer player) {
        List<PlayerItem> stacks = new ArrayList<PlayerItem>();
        addAll(stacks, player, player.inventory.mainInventory, 0);
        addAll(stacks, player, player.inventory.armorInventory, -1);
        try {
            IInventory baubles = BaublesApi.getBaubles(player);
            if (baubles != null) {
                for (int slot = 0; slot < baubles.getSizeInventory(); slot++) {
                    add(stacks, player, baubles.getStackInSlot(slot), -1);
                }
            }
        } catch (Throwable ignored) {
        }
        return stacks;
    }

    private static void addAll(List<PlayerItem> stacks, EntityPlayer player,
                               ItemStack[] inventory, int slotOffset) {
        for (int slot = 0; slot < inventory.length; slot++) {
            add(stacks, player, inventory[slot],
                    slotOffset < 0 ? -1 : slotOffset + slot);
        }
    }

    private static void add(List<PlayerItem> stacks, EntityPlayer player,
                            ItemStack stack, int mainInventorySlot) {
        if (stack != null && stack.stackSize > 0) {
            stacks.add(new PlayerItem(player, stack, mainInventorySlot));
        }
    }

    public static final class PlayerItem {

        private final EntityPlayer player;
        private final ItemStack stack;
        private final int mainInventorySlot;

        private PlayerItem(EntityPlayer player, ItemStack stack, int mainInventorySlot) {
            this.player = player;
            this.stack = stack;
            this.mainInventorySlot = mainInventorySlot;
        }

        public EntityPlayer getPlayer() {
            return player;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int getMainInventorySlot() {
            return mainInventorySlot;
        }

        public boolean isCurrentlyHeld() {
            return mainInventorySlot >= 0
                    && player.inventory.currentItem == mainInventorySlot
                    && player.getHeldItem() == stack;
        }
    }
}
