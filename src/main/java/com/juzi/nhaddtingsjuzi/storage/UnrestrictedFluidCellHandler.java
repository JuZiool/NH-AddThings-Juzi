package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.storage.MEInventoryHandler;

public class UnrestrictedFluidCellHandler extends MEInventoryHandler<IAEFluidStack> implements ICellCacheRegistry {
    public UnrestrictedFluidCellHandler(IMEInventory<IAEFluidStack> inventory) {
        super(inventory, StorageChannel.FLUIDS);
    }
    @Override public boolean canGetInv() { return true; }

    private UnrestrictedFluidCellInventory inventory() { return (UnrestrictedFluidCellInventory) getInternal(); }
    @Override public long getTotalBytes() { return inventory().getTotalBytes(); }
    @Override public long getFreeBytes() { return inventory().getFreeBytes(); }
    @Override public long getUsedBytes() { return inventory().getUsedBytes(); }
    @Override public long getTotalTypes() { return inventory().getTotalFluidTypes(); }
    @Override public long getFreeTypes() { return inventory().getRemainingFluidTypes(); }
    @Override public long getUsedTypes() { return inventory().getStoredFluidTypes(); }
    @Override public int getCellStatus() { return inventory().getStatusForCell(); }
    @Override public TYPE getCellType() { return TYPE.FLUID; }
}
