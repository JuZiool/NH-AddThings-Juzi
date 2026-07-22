package com.asdflj.ae2thing.client.gui.container;

import com.asdflj.ae2thing.client.gui.container.slot.SlotTicCraftingTerm;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotRestrictedInput;
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
    private final SlotRestrictedInput[] viewCells = new SlotRestrictedInput[5];
    private final SlotTicCraftingTerm outputSlot;
    private boolean canAccessViewCells;

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

        // Match AE ContainerMEMonitorable view-cell layout (x=206, y=8+i*18).
        IInventory viewCellInv = terminal.getViewCellStorage();
        for (int i = 0; i < viewCells.length; i++) {
            viewCells[i] = new SlotRestrictedInput(
                    SlotRestrictedInput.PlacableItemType.VIEW_CELL,
                    viewCellInv,
                    i,
                    206,
                    8 + i * 18,
                    inventory);
            viewCells[i].setAllowEdit(canAccessViewCells);
            addSlotToContainer(viewCells[i]);
        }

        bindPlayerInventory(inventory, 0, 0);
        onCraftMatrixChanged(crafting);
    }

    @Override
    void setMonitor() {
        monitor.setMonitor(host.getItemInventory());
        fluidMonitor.setMonitor(host.getFluidInventory(), host.getItemInventory());
        // Required so craftable-only fluid/aspect entries are forwarded into fluid updates.
        monitor.setFluidMonitorObject(fluidMonitor);
        if (monitor.getMonitor() != null) {
            monitor.addListener();
            setCellInventory(monitor.getMonitor());
        } else {
            setValidContainer(false);
        }
        if (fluidMonitor.getMonitor() != null) {
            fluidMonitor.addListener();
        }
        refreshNetworkPower();
    }

    /** Re-resolve grid energy after reconnect; safe to call every tick. */
    private void refreshNetworkPower() {
        if (!(host instanceof PartDualTerminal)) return;
        PartDualTerminal part = (PartDualTerminal) host;
        networkNode = part.getGridNode();
        if (networkNode == null) return;
        try {
            setPowerSource(new ChannelPowerSrc(networkNode, part.getProxy().getEnergy()));
        } catch (GridAccessException ignored) {
            // Retry on later detectAndSendChanges ticks once the grid is back.
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            // Keep CRAFT permission in sync while the GUI is open (security card changes).
            verifyPermissions(SecurityPermissions.CRAFT, false);

            boolean previous = canAccessViewCells;
            canAccessViewCells = hasAccess(SecurityPermissions.BUILD, false);
            if (previous != canAccessViewCells) {
                for (SlotRestrictedInput slot : viewCells) {
                    if (slot != null) slot.setAllowEdit(canAccessViewCells);
                }
            }

            // Retry power/monitor wiring if the part reconnected after open.
            if (getPowerSource() == null || networkNode == null || !networkNode.isActive()) {
                refreshNetworkPower();
            }
            if (monitor.getMonitor() == null && host != null) {
                monitor.setMonitor(host.getItemInventory());
                fluidMonitor.setMonitor(host.getFluidInventory(), host.getItemInventory());
                monitor.setFluidMonitorObject(fluidMonitor);
                if (monitor.getMonitor() != null) {
                    monitor.addListener();
                    setCellInventory(monitor.getMonitor());
                    setValidContainer(true);
                }
                if (fluidMonitor.getMonitor() != null) {
                    fluidMonitor.addListener();
                }
            }

            fluidMonitor.processItemList();
        }
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
    public ItemStack[] getViewCells() {
        ItemStack[] cells = new ItemStack[viewCells.length];
        for (int i = 0; i < viewCells.length; i++) {
            cells[i] = viewCells[i] == null ? null : viewCells[i].getStack();
        }
        return cells;
    }

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
