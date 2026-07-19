package com.juzi.nhaddtingsjuzi.storage;

import java.util.ArrayList;
import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.exceptions.AppEngException;
import com.glodblock.github.common.storage.IFluidCellInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class UnrestrictedFluidCellInventory implements IMEInventory<IAEFluidStack>, IFluidCellInventory {
    private final ItemStack cell;
    private final ISaveProvider provider;
    private final UnrestrictedFluidCellItem type;
    private CellFluidDataStorage storage;

    public UnrestrictedFluidCellInventory(ItemStack cell, ISaveProvider provider, UnrestrictedFluidCellItem type) throws AppEngException {
        if (cell == null || !(cell.getItem() instanceof UnrestrictedFluidCellItem)) throw new AppEngException("Not an unrestricted fluid cell");
        this.cell = cell;
        this.provider = provider;
        this.type = type;
    }

    UnrestrictedFluidCellInventory(ItemStack cell, ISaveProvider provider, UnrestrictedFluidCellItem type,
        CellFluidDataStorage storage) throws AppEngException {
        this(cell, provider, type);
        this.storage = storage;
    }

    public static UnrestrictedFluidCellInventory read(ItemStack stack, UnrestrictedFluidCellItem type) {
        try { return new UnrestrictedFluidCellInventory(stack, null, type); }
        catch (AppEngException e) { throw new IllegalArgumentException(e); }
    }

    private IItemList<IAEFluidStack> contents() {
        CellFluidDataStorage data = storage();
        return data == null ? appeng.api.AEApi.instance().storage().createFluidList() : data.getFluids();
    }

    private void save() {
        if (storage != null && provider != null) CellFluidStorageAccess.save(cell, provider, storage);
        if (provider != null) provider.saveChanges(this);
    }

    @Override public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if (input == null || input.getStackSize() <= 0 || type.isBlackListed(cell, input)) return input;
        IAEFluidStack stored = contents().findPrecise(input);
        long available = getRemainingFluidCount();
        if (stored != null) {
            long accepted = Math.min(input.getStackSize(), available);
            if (accepted <= 0) return input;
            if (mode == Actionable.MODULATE) { storage().insert(input, accepted); save(); }
            return accepted == input.getStackSize() ? null : copyWithSize(input, input.getStackSize() - accepted);
        }
        long accepted = Math.min(input.getStackSize(), available);
        if (accepted <= 0) return input;
        if (mode == Actionable.MODULATE) { storage().insert(input, accepted); save(); }
        return accepted == input.getStackSize() ? null : copyWithSize(input, input.getStackSize() - accepted);
    }

    private IAEFluidStack copyWithSize(IAEFluidStack source, long size) { IAEFluidStack result = source.copy(); result.setStackSize(size); return result; }

    @Override public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if (request == null || request.getStackSize() <= 0) return null;
        IAEFluidStack stored = contents().findPrecise(request);
        if (stored == null) return null;
        long amount = Math.min(request.getStackSize(), stored.getStackSize());
        IAEFluidStack result = copyWithSize(stored, amount);
        if (mode == Actionable.MODULATE) { storage().extract(request, amount); save(); }
        return result;
    }

    @Override public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) { for (IAEFluidStack stack : contents()) out.add(stack); return out; }
    @Override public IAEFluidStack getAvailableItem(IAEFluidStack request) { IAEFluidStack stack = contents().findPrecise(request); return stack == null ? null : stack.copy(); }
    @Override public StorageChannel getChannel() { return StorageChannel.FLUIDS; }
    @Override public ItemStack getItemStack() { return cell; }
    @Override public double getIdleDrain(ItemStack is) { return type.getIdleDrain(is); }
    @Override public IInventory getConfigInventory() { return type.getConfigInventory(cell); }
    /** GTO-style cells charge capacity by total fluid amount and never by fluid type. */
    @Override public int getBytesPerType() { return 0; }
    @Override public long getTotalBytes() { return type.getCapacity(); }
    @Override public long getFreeBytes() { return Math.max(0, getTotalBytes() - getUsedBytes()); }
    @Override public long getUsedBytes() { return UnrestrictedFluidCellStorageLogic.usedBytes(getStoredFluidCount()); }
    @Override public long getStoredFluidCount() { return storage() == null ? 0 : storage().getStoredFluidCount(); }
    @Override public long getRemainingFluidCount() {
        return UnrestrictedFluidCellStorageLogic.remainingAmount(getTotalBytes(), getStoredFluidCount());
    }
    @Override public long getRemainingFluidCountDist(IAEFluidStack stack) { return getRemainingFluidCount(); }
    @Override public long getRemainingFluidTypes() { return Long.MAX_VALUE; }
    @Override public int getUnusedFluidCount() { return 0; }
    @Override public boolean canHoldNewFluid() { return getRemainingFluidCount() > 0; }
    @Override public int getStatusForCell() {
        if (getStoredFluidCount() == 0) return 1;
        return canHoldNewFluid() ? 2 : (getRemainingFluidCount() > 0 ? 3 : 4);
    }
    @Override public long getStoredFluidTypes() { return storage() == null ? 0 : storage().getStoredFluidTypes(); }
    @Override public long getTotalFluidTypes() { return Integer.MAX_VALUE; }
    @Override public List<IAEFluidStack> getContents() { List<IAEFluidStack> result = new ArrayList<IAEFluidStack>(); for (IAEFluidStack stack : contents()) result.add(stack); return result; }
    @Override public IInventory getUpgradesInventory() { return type.getUpgradesInventory(cell); }

    private CellFluidDataStorage storage() {
        if (storage == null) storage = CellFluidStorageAccess.get(cell, provider);
        return storage;
    }
}
