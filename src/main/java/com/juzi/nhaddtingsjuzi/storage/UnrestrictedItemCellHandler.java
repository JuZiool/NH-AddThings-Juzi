package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.storage.MEInventoryHandler;

public class UnrestrictedItemCellHandler extends MEInventoryHandler<IAEItemStack> implements ICellCacheRegistry {
    private final UnrestrictedItemCellInventory cellInventory;

    public UnrestrictedItemCellHandler(UnrestrictedItemCellInventory inventory) {
        super(inventory, StorageChannel.ITEMS);
        this.cellInventory = inventory;
    }

    @Override
    public boolean canGetInv() {
        return true;
    }

    UnrestrictedItemCellInventory inventory() {
        return cellInventory;
    }

    @Override
    public long getTotalBytes() {
        return cellInventory.getTotalBytes();
    }

    @Override
    public long getFreeBytes() {
        return cellInventory.getFreeBytes();
    }

    @Override
    public long getUsedBytes() {
        return cellInventory.getUsedBytes();
    }

    @Override
    public long getTotalTypes() {
        return cellInventory.getTotalItemTypes();
    }

    @Override
    public long getFreeTypes() {
        return cellInventory.getRemainingItemTypes();
    }

    @Override
    public long getUsedTypes() {
        return cellInventory.getStoredItemTypes();
    }

    @Override
    public int getCellStatus() {
        return cellInventory.getStatusForCell();
    }

    @Override
    public TYPE getCellType() {
        return TYPE.ITEM;
    }
}
