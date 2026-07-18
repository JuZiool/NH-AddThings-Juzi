package com.juzi.nhaddtingsjuzi.storage;

import java.util.List;

import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import com.glodblock.github.common.storage.IStorageFluidCell;
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

public class UnrestrictedFluidCellItem extends Item implements IStorageFluidCell, CellInventoryProvider {
    private final int capacity;
    private final String id;

    public UnrestrictedFluidCellItem(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        ((Item) this).setMaxStackSize(1);
        ((Item) this).setUnlocalizedName(id);
        ((Item) this).setTextureName(NHAddTingsJuzi.MODID + ":" + id);
        setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
    }

    public void register() { GameRegistry.registerItem(this, id); }
    public int getCapacity() { return capacity; }
    @Override public long getBytes(ItemStack cellItem) { return capacity; }
    @Override public int getBytesPerType(ItemStack cellItem) { return 8; }
    @Override public boolean isBlackListed(ItemStack cellItem, IAEFluidStack fluid) {
        return fluid == null || fluid.getFluid() == null;
    }
    @Override public boolean storableInStorageCell() { return false; }
    @Override public boolean isStorageCell(ItemStack i) { return false; }
    @Override public double getIdleDrain(ItemStack is) { return 1.0D; }
    @Override public int getTotalTypes(ItemStack cellItem) { return Integer.MAX_VALUE; }
    @Override public boolean isEditable(ItemStack is) { return true; }
    @Override public IInventory getUpgradesInventory(ItemStack is) { return new appeng.items.contents.CellUpgrades(is, 0); }
    @Override public IInventory getConfigInventory(ItemStack is) { return new appeng.items.contents.CellConfig(is); }
    @Override public FuzzyMode getFuzzyMode(ItemStack is) { return FuzzyMode.IGNORE_ALL; }
    @Override public void setFuzzyMode(ItemStack is, FuzzyMode mode) { }
    public IMEInventoryHandler<IAEFluidStack> getInventoryHandler(ItemStack is, ISaveProvider provider, EntityPlayer player)
        throws AppEngException { return new UnrestrictedFluidCellHandler(new UnrestrictedFluidCellInventory(is, provider, this)); }

    @Override
    public IMEInventoryHandler<?> getCellInventory(ItemStack is, ISaveProvider provider, EntityPlayer player)
        throws AppEngException {
        return getInventoryHandler(is, provider, player);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!player.isSneaking() || world.isRemote) {
            return super.onItemRightClick(stack, world, player);
        }
        if (!CellFluidStorageAccess.isEmpty(stack, null)) {
            return stack;
        }

        if (stack.stackSize <= 1) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            stack.stackSize = 0;
        } else {
            stack.stackSize--;
        }
        giveOrDrop(player, new ItemStack(com.juzi.nhaddtingsjuzi.registry.ModItems.unrestrictedShell));
        giveOrDrop(player, UnrestrictedFluidCellComponents.forCapacity(capacity));
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
        UnrestrictedFluidCellInventory inv = UnrestrictedFluidCellInventory.read(stack, this);
        lines.add(StatCollector.translateToLocalFormatted("nh_addtings_juzi.cell.types", CellFluidStorageAccess.getStoredFluidTypes(stack)));
        lines.add(StatCollector.translateToLocalFormatted("nh_addtings_juzi.cell.space", CellFluidStorageAccess.getUsedBytes(stack), capacity));
    }

    public static UnrestrictedFluidCellItem create(String id, int capacity) {
        UnrestrictedFluidCellItem item = new UnrestrictedFluidCellItem(id, capacity);
        item.register();
        return item;
    }
}
