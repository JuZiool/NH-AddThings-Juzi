package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.storage.MEInventoryHandler;

public class UnrestrictedFluidCellHandler extends MEInventoryHandler<IAEFluidStack> implements ICellCacheRegistry {
    private final UnrestrictedFluidCellInventory cellInventory;

    public UnrestrictedFluidCellHandler(UnrestrictedFluidCellInventory inventory) {
        super(inventory, StorageChannel.FLUIDS);
        this.cellInventory = inventory;
    }

    @Override
    public boolean canGetInv() {
        return true;
    }

    UnrestrictedFluidCellInventory inventory() {
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
        return cellInventory.getTotalFluidTypes();
    }

    @Override
    public long getFreeTypes() {
        return cellInventory.getRemainingFluidTypes();
    }

    @Override
    public long getUsedTypes() {
        return cellInventory.getStoredFluidTypes();
    }

    @Override
    public int getCellStatus() {
        return cellInventory.getStatusForCell();
    }

    @Override
    public TYPE getCellType() {
        return TYPE.FLUID;
    }
}
