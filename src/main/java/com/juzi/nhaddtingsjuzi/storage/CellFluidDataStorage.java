package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEFluidStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.UUID;

/** One UUID-backed fluid collection used by an unrestricted fluid cell. */
public final class CellFluidDataStorage {
    private final UUID uuid;
    private IItemList<IAEFluidStack> fluids;
    private long storedFluidCount;

    public CellFluidDataStorage(UUID uuid) {
        this.uuid = uuid;
        this.fluids = AEApi.instance().storage().createFluidList();
    }

    public UUID getUuid() {
        return uuid;
    }

    public IItemList<IAEFluidStack> getFluids() {
        return fluids;
    }

    public IAEFluidStack find(IAEFluidStack stack) {
        return fluids.findPrecise(stack);
    }

    public void addImported(IAEFluidStack stack) {
        if (stack == null || stack.getStackSize() <= 0) return;
        fluids.add(stack);
        storedFluidCount += stack.getStackSize();
    }

    public long insert(IAEFluidStack stack, long amount) {
        if (stack == null || amount <= 0) return 0;
        IAEFluidStack copy = stack.copy();
        copy.setStackSize(amount);
        fluids.add(copy);
        storedFluidCount += amount;
        return amount;
    }

    public long extract(IAEFluidStack stack, long amount) {
        if (stack == null || amount <= 0) return 0;
        IAEFluidStack stored = find(stack);
        if (stored == null) return 0;
        long extracted = Math.min(amount, stored.getStackSize());
        stored.decStackSize(extracted);
        storedFluidCount -= extracted;
        if (stored.getStackSize() <= 0) removeEmpty(stored);
        return extracted;
    }

    public long getStoredFluidCount() {
        return storedFluidCount;
    }

    public long getStoredFluidTypes() {
        return fluids.size();
    }

    public long getUsedBytes() {
        return UnrestrictedFluidCellStorageLogic.usedBytes(storedFluidCount);
    }

    public void readFromNBT(NBTTagList list) {
        fluids = AEApi.instance().storage().createFluidList();
        storedFluidCount = 0;
        if (list == null) return;
        for (int i = 0; i < list.tagCount(); i++) {
            IAEFluidStack stack = AEFluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i));
            addImported(stack);
        }
    }

    public NBTTagList writeToNBT() {
        NBTTagList list = new NBTTagList();
        for (IAEFluidStack stack : fluids) {
            if (stack.getStackSize() <= 0) continue;
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            list.appendTag(tag);
        }
        return list;
    }

    private void removeEmpty(IAEFluidStack removed) {
        IItemList<IAEFluidStack> remaining = AEApi.instance().storage().createFluidList();
        for (IAEFluidStack stack : fluids) {
            if (stack != removed && stack.getStackSize() > 0) remaining.add(stack);
        }
        fluids = remaining;
    }
}
