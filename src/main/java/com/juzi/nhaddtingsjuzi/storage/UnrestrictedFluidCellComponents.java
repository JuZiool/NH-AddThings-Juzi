package com.juzi.nhaddtingsjuzi.storage;

import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.loader.ItemAndBlockHolder;

import net.minecraft.item.ItemStack;

/** Resolves the AE2 Fluid Crafting storage component for each fluid cell tier. */
public final class UnrestrictedFluidCellComponents {

    private UnrestrictedFluidCellComponents() {}

    public static int damageForCapacity(int capacity) {
        switch (capacity) {
            case 1024:
                return CellType.Cell1kPart.getDamageValue();
            case 4096:
                return CellType.Cell4kPart.getDamageValue();
            case 16384:
                return CellType.Cell16kPart.getDamageValue();
            case 65536:
                return CellType.Cell64kPart.getDamageValue();
            default:
                throw new IllegalArgumentException("Unsupported unrestricted fluid cell capacity: " + capacity);
        }
    }

    public static ItemStack forCapacity(int capacity) {
        int damage = damageForCapacity(capacity);
        if (ItemAndBlockHolder.CELL_PART == null) return null;
        return new ItemStack(ItemAndBlockHolder.CELL_PART, 1, damage);
    }
}
