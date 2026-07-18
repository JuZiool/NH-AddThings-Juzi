package com.juzi.nhaddtingsjuzi.storage;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public class UnrestrictedCellHandler implements ICellHandler {
    @Override public boolean isCell(ItemStack stack) { return stack != null && (stack.getItem() instanceof UnrestrictedCellItem || stack.getItem() instanceof UnrestrictedFluidCellItem); }
    @Override public IMEInventoryHandler<?> getCellInventory(ItemStack stack, ISaveProvider provider, StorageChannel channel) {
        if (stack == null) return null;
        if (stack.getItem() instanceof CellInventoryProvider) {
            CellInventoryProvider item = (CellInventoryProvider) stack.getItem();
            if ((item instanceof UnrestrictedCellItem && channel != StorageChannel.ITEMS)
                || (item instanceof UnrestrictedFluidCellItem && channel != StorageChannel.FLUIDS)) return null;
            try { return item.getCellInventory(stack, provider, null); }
            catch (Exception ignored) { return null; }
        }
        return null;
    }
    @Override public IIcon getTopTexture_Light() { return ExtraBlockTextures.BlockMEChestItems_Light.getIcon(); }
    @Override public IIcon getTopTexture_Medium() { return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon(); }
    @Override public IIcon getTopTexture_Dark() { return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon(); }
    @Override public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler handler, IMEInventoryHandler inv, ItemStack stack, StorageChannel channel) {
        if (chest instanceof TileEntity && channel == StorageChannel.ITEMS) {
            Platform.openGUI(player, (TileEntity) chest, chest.getUp(), GuiBridge.GUI_ME);
        }
    }
    @Override public int getStatusForCell(ItemStack stack, IMEInventory inventory) { return 1; }
    @Override public double cellIdleDrain(ItemStack stack, IMEInventory inventory) { return 1.0D; }
}
