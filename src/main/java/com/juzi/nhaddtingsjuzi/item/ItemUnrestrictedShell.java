package com.juzi.nhaddtingsjuzi.item;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;

import net.minecraft.item.Item;

/** Base crafting component for the unrestricted storage cells. */
public class ItemUnrestrictedShell extends Item {

    public ItemUnrestrictedShell() {
        ((Item) this).setUnlocalizedName("unrestricted_shell");
        ((Item) this).setTextureName(NHAddTingsJuzi.MODID + ":unrestricted_shell");
        ((Item) this).setMaxStackSize(64);
        setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
    }
}
