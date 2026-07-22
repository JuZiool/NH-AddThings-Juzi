package com.juzi.nhaddtingsjuzi.terminal.network;

import com.asdflj.ae2thing.client.gui.container.ContainerJuziDualTerminal;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.IPartitionList;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

/** Server-side equivalent of AE2Things' NEI recipe filler for the placed mixed terminal. */
public final class DualTerminalRecipeHandler
        implements IMessageHandler<DualTerminalRecipeMessage, IMessage> {

    @Override
    public IMessage onMessage(DualTerminalRecipeMessage message, MessageContext context) {
        EntityPlayerMP player = context.getServerHandler().playerEntity;
        if (!(player.openContainer instanceof ContainerJuziDualTerminal)) return null;

        ItemStack[][] recipe = unpackRecipe(message.getIngredients());
        fillCraftingGrid(player, (ContainerJuziDualTerminal) player.openContainer, recipe);
        return null;
    }

    private static ItemStack[][] unpackRecipe(NBTTagCompound tag) {
        ItemStack[][] recipe = new ItemStack[9][];
        if (tag == null) return recipe;

        for (int slot = 0; slot < recipe.length; slot++) {
            NBTTagList alternatives = tag.getTagList("#" + slot, 10);
            int count = Math.min(alternatives.tagCount(), 256);
            if (count == 0) continue;

            ItemStack[] stacks = new ItemStack[count];
            int valid = 0;
            for (int index = 0; index < count; index++) {
                ItemStack stack = ItemStack.loadItemStackFromNBT(
                        alternatives.getCompoundTagAt(index));
                if (stack != null) stacks[valid++] = stack;
            }
            if (valid == 0) continue;
            if (valid == stacks.length) {
                recipe[slot] = stacks;
            } else {
                ItemStack[] compact = new ItemStack[valid];
                System.arraycopy(stacks, 0, compact, 0, valid);
                recipe[slot] = compact;
            }
        }
        return recipe;
    }

    private static void fillCraftingGrid(EntityPlayerMP player,
            ContainerJuziDualTerminal container, ItemStack[][] ingredients) {
        if (ingredients == null) return;

        IInventory crafting = container.getInventoryByName("crafting");
        IInventory playerInventory = container.getInventoryByName("player");
        if (crafting == null || crafting.getSizeInventory() < 9) return;

        InventoryCrafting recipeMatrix = new InventoryCrafting(new ContainerNull(), 3, 3);
        for (int slot = 0; slot < 9; slot++) {
            if (ingredients[slot] != null && ingredients[slot].length > 0) {
                recipeMatrix.setInventorySlotContents(slot, ingredients[slot][0]);
            }
        }

        IRecipe recipe = Platform.findMatchingRecipe(recipeMatrix, player.worldObj);
        if (recipe == null) return;
        ItemStack expectedOutput = recipe.getCraftingResult(recipeMatrix);
        if (expectedOutput == null) return;

        IMEMonitor<IAEItemStack> monitor = container.getMonitor();
        IEnergySource power = container.getPowerSource();
        IItemList<IAEItemStack> storage = monitor == null ? null : monitor.getStorageList();
        IPartitionList<IAEItemStack> filter = ItemViewCell.createFilter(container.getViewCells());
        Actionable mode = container.useRealItems() ? Actionable.MODULATE : Actionable.SIMULATE;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack requested = recipeMatrix.getStackInSlot(slot);
            ItemStack current = crafting.getStackInSlot(slot);

            if (current != null && !stillProducesExpectedOutput(
                    recipe, recipeMatrix, player, slot, current, expectedOutput)) {
                IAEItemStack remainder;
                if (mode == Actionable.SIMULATE) {
                    remainder = null;
                } else if (monitor == null || power == null) {
                    remainder = AEItemStack.create(current);
                } else {
                    remainder = Platform.poweredInsert(power, monitor, AEItemStack.create(current),
                            container.getActionSource());
                }
                crafting.setInventorySlotContents(
                        slot, remainder == null ? null : remainder.getItemStack());
                current = crafting.getStackInSlot(slot);
            }

            if (requested == null || current != null) continue;

            ItemStack extracted = null;
            if (monitor != null && power != null && storage != null) {
                extracted = Platform.extractItemsByRecipe(
                        power,
                        container.getActionSource(),
                        monitor,
                        player.worldObj,
                        recipe,
                        expectedOutput,
                        recipeMatrix,
                        requested,
                        slot,
                        storage,
                        mode,
                        filter);

                if (extracted == null) {
                    extracted = extractAlternativeFromNetwork(
                            ingredients[slot], filter, power, monitor, container, mode);
                }
            }

            if (extracted == null && playerInventory != null) {
                extracted = extractFromPlayerInventory(player, mode, requested);
            }
            crafting.setInventorySlotContents(slot, extracted);
        }

        container.onCraftMatrixChanged(crafting);
    }

    private static boolean stillProducesExpectedOutput(IRecipe recipe,
            InventoryCrafting matrix, EntityPlayerMP player, int slot,
            ItemStack current, ItemStack expectedOutput) {
        ItemStack requested = matrix.getStackInSlot(slot);
        matrix.setInventorySlotContents(slot, current);
        ItemStack actual = recipe.matches(matrix, player.worldObj)
                ? recipe.getCraftingResult(matrix)
                : null;
        matrix.setInventorySlotContents(slot, requested);
        return actual != null && Platform.isSameItemPrecise(actual, expectedOutput);
    }

    private static ItemStack extractAlternativeFromNetwork(ItemStack[] alternatives,
            IPartitionList<IAEItemStack> filter, IEnergySource power,
            IMEMonitor<IAEItemStack> monitor, ContainerJuziDualTerminal container,
            Actionable mode) {
        if (alternatives == null) return null;
        for (ItemStack alternative : alternatives) {
            IAEItemStack request = AEItemStack.create(alternative);
            if (request == null || filter != null && !filter.isListed(request)) continue;
            request.setStackSize(1);

            if (mode == Actionable.SIMULATE) return request.getItemStack();
            IAEItemStack extracted = Platform.poweredExtraction(
                    power, monitor, request, container.getActionSource());
            if (extracted != null) return extracted.getItemStack();
        }
        return null;
    }

    private static ItemStack extractFromPlayerInventory(EntityPlayer player,
            Actionable mode, ItemStack requested) {
        InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
        AEItemStack aeRequested = AEItemStack.create(requested);
        if (adaptor == null || aeRequested == null) return null;

        boolean simulate = mode == Actionable.SIMULATE;
        boolean fuzzy = aeRequested.isOre()
                || requested.getItemDamage() == Short.MAX_VALUE
                || requested.hasTagCompound()
                || requested.isItemStackDamageable();
        if (!fuzzy) {
            return simulate
                    ? adaptor.simulateRemove(1, requested, null)
                    : adaptor.removeItems(1, requested, null);
        }
        return simulate
                ? adaptor.simulateSimilarRemove(1, requested, FuzzyMode.IGNORE_ALL, null)
                : adaptor.removeSimilarItems(1, requested, FuzzyMode.IGNORE_ALL, null);
    }
}
