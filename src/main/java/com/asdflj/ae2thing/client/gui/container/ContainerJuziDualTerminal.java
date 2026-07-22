package com.asdflj.ae2thing.client.gui.container;

import com.asdflj.ae2thing.client.gui.container.slot.SlotTicCraftingTerm;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.api.networking.IGridNode;
import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.me.GridAccessException;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

/** AE2Things mixed monitor adapted to a placed AE2 crafting-terminal part. */
public final class ContainerJuziDualTerminal extends ContainerMonitor {
    private final PartDualTerminal terminal;
    private final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
    private final SlotTicCraftingTerm outputSlot;

    public ContainerJuziDualTerminal(InventoryPlayer inventory, ITerminalHost host) {
        super(inventory, host);
        if (!(host instanceof PartDualTerminal)) {
            throw new IllegalArgumentException("Dual terminal host required");
        }
        terminal = (PartDualTerminal) host;
        IInventory crafting = terminal.getInventoryByName("crafting");
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                craftingSlots[x + y * 3] = new SlotCraftingMatrix(
                        this, crafting, x + y * 3, 37 + x * 18, -72 + y * 18);
                addSlotToContainer(craftingSlots[x + y * 3]);
            }
        }
        AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
        outputSlot = new SlotTicCraftingTerm(
                getPlayerInv().player,
                getActionSource(),
                getPowerSource(),
                terminal,
                crafting,
                crafting,
                output,
                131,
                -54,
                this);
        addSlotToContainer(outputSlot);
        bindPlayerInventory(inventory, 0, 0);
        onCraftMatrixChanged(crafting);
    }

    @Override
    void setMonitor() {
        monitor.setMonitor(host.getItemInventory());
        fluidMonitor.setMonitor(host.getFluidInventory(), host.getItemInventory());
        if (monitor.getMonitor() != null) {
            monitor.addListener();
            setCellInventory(monitor.getMonitor());
        }
        if (fluidMonitor.getMonitor() != null) fluidMonitor.addListener();
        if (host instanceof PartDualTerminal) {
            PartDualTerminal part = (PartDualTerminal) host;
            networkNode = part.getGridNode();
            try {
                setPowerSource(new ChannelPowerSrc(networkNode, part.getProxy().getEnergy()));
            } catch (GridAccessException ignored) {
                // The container will become valid once the terminal reconnects.
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) fluidMonitor.processItemList();
        super.detectAndSendChanges();
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        fluidMonitor.queueInventory(crafter);
    }

    @Override
    public void removeCraftingFromCrafters(ICrafting crafter) {
        super.removeCraftingFromCrafters(crafter);
        fluidMonitor.removeCraftingFromCrafters(crafter);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (fluidMonitor.getMonitor() != null) fluidMonitor.removeListener();
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        ContainerNull nullContainer = new ContainerNull();
        InventoryCrafting matrix = new InventoryCrafting(nullContainer, 3, 3);
        for (int slot = 0; slot < 9; slot++) {
            matrix.setInventorySlotContents(slot, craftingSlots[slot].getStack());
        }
        outputSlot.putStack(CraftingManager.getInstance().findMatchingRecipe(
                matrix, getPlayerInv().player.worldObj));
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if ("player".equals(name)) return getInventoryPlayer();
        return terminal.getInventoryByName(name);
    }

    @Override
    public IGridNode getNetworkNode() {
        return terminal == null ? networkNode : terminal.getGridNode();
    }

    @Override
    public boolean useRealItems() { return true; }

    @Override
    public ItemStack[] getViewCells() { return new ItemStack[0]; }

    @Override
    public void saveChanges() {
        if (terminal != null && terminal.getHost() != null) terminal.getHost().markForSave();
    }

    @Override
    public void onChangeInventory(IInventory inventory, int slot, InvOperation operation,
            ItemStack removedStack, ItemStack newStack) {
        // The one-slot output inventory is owned by this container. Calling
        // onCraftMatrixChanged here would update outputSlot again, recursively
        // re-entering this callback until the server stack overflows.
    }
}
