package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.AEApi;
import net.minecraft.item.ItemStack;

/** Resolves the matching Applied Energistics storage component for each cell tier. */
public final class UnrestrictedCellComponents {

    private UnrestrictedCellComponents() {}

    public static ItemStack forCapacity(int capacity) {
        switch (capacity) {
            case 1024:
                return require("1k", AEApi.instance().definitions().materials().cell1kPart().maybeStack(1).orNull());
            case 4096:
                return require("4k", AEApi.instance().definitions().materials().cell4kPart().maybeStack(1).orNull());
            case 16384:
                return require("16k", AEApi.instance().definitions().materials().cell16kPart().maybeStack(1).orNull());
            case 65536:
                return require("64k", AEApi.instance().definitions().materials().cell64kPart().maybeStack(1).orNull());
            default:
                throw new IllegalArgumentException("Unsupported unrestricted item cell capacity: " + capacity);
        }
    }

    private static ItemStack require(String tier, ItemStack stack) {
        if (stack == null) {
            throw new IllegalStateException("Missing AE2 " + tier + " storage component");
        }
        return stack;
    }
}
