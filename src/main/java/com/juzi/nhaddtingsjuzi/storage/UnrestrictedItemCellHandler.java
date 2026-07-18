package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.storage.MEInventoryHandler;

public class UnrestrictedItemCellHandler extends MEInventoryHandler<IAEItemStack> implements ICellCacheRegistry {
    public UnrestrictedItemCellHandler(IMEInventory<IAEItemStack> inventory) {
        super(inventory, StorageChannel.ITEMS);
    }
    @Override public boolean canGetInv() { return true; }

    private UnrestrictedItemCellInventory inventory() { return (UnrestrictedItemCellInventory) getInternal(); }
    @Override public long getTotalBytes() { return inventory().getTotalBytes(); }
    @Override public long getFreeBytes() { return inventory().getFreeBytes(); }
    @Override public long getUsedBytes() { return inventory().getUsedBytes(); }
    @Override public long getTotalTypes() { return inventory().getTotalItemTypes(); }
    @Override public long getFreeTypes() { return inventory().getRemainingItemTypes(); }
    @Override public long getUsedTypes() { return inventory().getStoredItemTypes(); }
    @Override public int getCellStatus() { return inventory().getStatusForCell(); }
    @Override public TYPE getCellType() { return TYPE.ITEM; }
}
