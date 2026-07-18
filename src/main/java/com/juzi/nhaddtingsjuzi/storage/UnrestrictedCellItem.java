package com.juzi.nhaddtingsjuzi.storage;

import java.util.List;

import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.registry.ModItems;

public class UnrestrictedCellItem extends Item implements IStorageCell, CellInventoryProvider {
    private final int capacity;
    private final String id;

    public UnrestrictedCellItem(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        ((Item) this).setMaxStackSize(1);
        ((Item) this).setUnlocalizedName(id);
        ((Item) this).setTextureName(NHAddTingsJuzi.MODID + ":" + id);
        setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
    }

    public void register() {
        GameRegistry.registerItem(this, id);
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getBytes(ItemStack cellItem) { return capacity; }

    @Override
    public long getBytesLong(ItemStack cellItem) { return capacity; }

    @Override
    public int BytePerType(ItemStack cellItem) { return 8; }

    @Override
    public int getBytesPerType(ItemStack cellItem) { return 8; }

    @Override
    public int getTotalTypes(ItemStack cellItem) { return Integer.MAX_VALUE; }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        return requestedAddition == null || requestedAddition.getItem() == this;
    }

    @Override public boolean storableInStorageCell() { return false; }
    @Override public boolean isStorageCell(ItemStack i) { return false; }
    @Override public double getIdleDrain() { return 1.0D; }
    @Override public double getIdleDrain(ItemStack is) { return getIdleDrain(); }
    @Override public boolean isEditable(ItemStack is) { return true; }
    @Override public IInventory getUpgradesInventory(ItemStack is) { return new appeng.items.contents.CellUpgrades(is, 0); }
    @Override public IInventory getConfigInventory(ItemStack is) { return new appeng.items.contents.CellConfig(is); }
    @Override public FuzzyMode getFuzzyMode(ItemStack is) { return FuzzyMode.IGNORE_ALL; }
    @Override public void setFuzzyMode(ItemStack is, FuzzyMode mode) { }

    public IMEInventoryHandler<IAEItemStack> getInventoryHandler(ItemStack is, ISaveProvider provider, EntityPlayer player)
        throws AppEngException {
        return new UnrestrictedItemCellHandler(new UnrestrictedItemCellInventory(is, provider, this));
    }

    @Override
    public IMEInventoryHandler<?> getCellInventory(ItemStack is, ISaveProvider provider, EntityPlayer player)
        throws AppEngException {
        return getInventoryHandler(is, provider, player);
    }

    @Override public String getOreFilter(ItemStack is) { return ""; }
    public StorageChannel getChannel() { return StorageChannel.ITEMS; }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!player.isSneaking() || world.isRemote) {
            return super.onItemRightClick(stack, world, player);
        }
        if (!CellStorageAccess.isEmpty(stack, null)) {
            return stack;
        }

        if (stack.stackSize <= 1) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            stack.stackSize = 0;
        } else {
            stack.stackSize--;
        }
        giveOrDrop(player, new ItemStack(ModItems.unrestrictedShell));
        giveOrDrop(player, UnrestrictedCellComponents.forCapacity(capacity));
        // Forge 1.7.10 reads stackSize from the returned stack without a null check.
        return stack;
    }

    private static void giveOrDrop(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack.copy())) {
            player.entityDropItem(stack, 0.0F);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        super.addInformation(stack, player, lines, advanced);
        lines.add(StatCollector.translateToLocalFormatted("nh_addtings_juzi.cell.types", CellStorageAccess.getStoredItemTypes(stack)));
        lines.add(StatCollector.translateToLocalFormatted("nh_addtings_juzi.cell.space", CellStorageAccess.getUsedBytes(stack), capacity));
    }

    public static UnrestrictedCellItem create(String id, int capacity) {
        UnrestrictedCellItem item = new UnrestrictedCellItem(id, capacity);
        item.register();
        return item;
    }
}
