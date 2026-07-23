package com.juzi.nhaddtingsjuzi.network;

import java.util.List;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.terminal.network.DualTerminalRecipeHandler;
import com.juzi.nhaddtingsjuzi.terminal.network.DualTerminalRecipeMessage;
import com.juzi.nhaddtingsjuzi.network.DualTerminalCraftRequestHandler;
import com.juzi.nhaddtingsjuzi.network.DualTerminalCraftRequestMessage;
import com.juzi.nhaddtingsjuzi.network.DualTerminalReturnHandler;
import com.juzi.nhaddtingsjuzi.network.DualTerminalReturnMessage;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.item.ElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class ModNetwork {

    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE
            .newSimpleChannel(NHAddTingsJuzi.MODID);
    private static boolean registered;

    private ModNetwork() {}

    public static void register() {
        if (registered) {
            return;
        }
        CHANNEL.registerMessage(ServerMessageHandler.class,
                HeldItemChargeMessage.class, 0, Side.SERVER);
        CHANNEL.registerMessage(DualTerminalRecipeHandler.class,
                DualTerminalRecipeMessage.class, 1, Side.SERVER);
        CHANNEL.registerMessage(DualTerminalCraftRequestHandler.class,
                DualTerminalCraftRequestMessage.class, 2, Side.SERVER);
        CHANNEL.registerMessage(DualTerminalReturnHandler.class,
                DualTerminalReturnMessage.class, 3, Side.SERVER);
        registered = true;
    }

    @SideOnly(Side.CLIENT)
    public static void registerClient() {
        CHANNEL.registerMessage(HeldItemChargeHandler.class,
                HeldItemChargeMessage.class, 0, Side.CLIENT);
    }

    static SimpleNetworkWrapper getChannel() {
        return CHANNEL;
    }

    @SideOnly(Side.CLIENT)
    public static void sendDualTerminalRecipe(NBTTagCompound ingredients) {
        CHANNEL.sendToServer(new DualTerminalRecipeMessage(ingredients));
    }

    @SideOnly(Side.CLIENT)
    public static void sendDualTerminalCraftRequest(int amount, boolean heldShift) {
        CHANNEL.sendToServer(new DualTerminalCraftRequestMessage(amount, heldShift));
    }

    @SideOnly(Side.CLIENT)
    public static void sendDualTerminalReturnRequest() {
        CHANNEL.sendToServer(new DualTerminalReturnMessage());
    }

    public static void syncHeldItem(EntityPlayerMP player, int inventorySlot,
                                    ItemStack stack, double acceptedCharge) {
        if (inventorySlot < 0 || player.inventory.currentItem != inventorySlot) {
            return;
        }

        updateContainerSnapshot(player.openContainer, player, inventorySlot, stack);
        if (player.inventoryContainer != player.openContainer) {
            updateContainerSnapshot(player.inventoryContainer, player, inventorySlot, stack);
        }
        CHANNEL.sendTo(new HeldItemChargeMessage(
                inventorySlot, stack, acceptedCharge), player);
    }

    @SuppressWarnings("unchecked")
    private static void updateContainerSnapshot(Container container,
                                                EntityPlayerMP player,
                                                int inventorySlot,
                                                ItemStack stack) {
        Slot slot = container.getSlotFromInventory(player.inventory, inventorySlot);
        if (slot == null) {
            return;
        }
        List<ItemStack> snapshots = container.inventoryItemStacks;
        if (slot.slotNumber >= 0 && slot.slotNumber < snapshots.size()) {
            snapshots.set(slot.slotNumber, stack.copy());
        }
    }

    public static final class ServerMessageHandler
            implements IMessageHandler<HeldItemChargeMessage, IMessage> {

        @Override
        public IMessage onMessage(HeldItemChargeMessage message, MessageContext context) {
            return null;
        }
    }
}
