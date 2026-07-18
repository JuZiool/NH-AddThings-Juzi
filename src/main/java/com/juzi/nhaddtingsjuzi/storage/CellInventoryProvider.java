package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.exceptions.AppEngException;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface CellInventoryProvider {
    IMEInventoryHandler<?> getCellInventory(ItemStack stack, ISaveProvider provider, EntityPlayer player) throws AppEngException;
}
