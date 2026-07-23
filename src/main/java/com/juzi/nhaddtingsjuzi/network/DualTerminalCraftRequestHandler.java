package com.juzi.nhaddtingsjuzi.network;

import com.juzi.nhaddtingsjuzi.terminal.network.DualTerminalGuiHandler;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

/** Completes the amount dialog without AE2Things miscasting the dual part as a wireless terminal. */
public final class DualTerminalCraftRequestHandler
        implements IMessageHandler<DualTerminalCraftRequestMessage, IMessage> {

    @Override
    public IMessage onMessage(DualTerminalCraftRequestMessage message, MessageContext context) {
        EntityPlayerMP player = context.getServerHandler().playerEntity;
        if (player == null || message == null || message.getAmount() <= 0) return null;
        if (!(player.openContainer instanceof ContainerCraftAmount)) return null;

        ContainerCraftAmount amount = (ContainerCraftAmount) player.openContainer;
        if (!(amount.getTarget() instanceof PartDualTerminal)) return null;

        IAEItemStack item = amount.getItemToCraft();
        ContainerOpenContext openContext = amount.getOpenContext();
        TileEntity tile = openContext == null ? null : openContext.getTile();
        if (item == null || tile == null || openContext.getSide() == null
                || DualTerminalGuiHandler.findPart(tile, openContext.getSide()) == null) {
            return null;
        }

        item.setStackSize(message.getAmount());
        amount.setItemToCraft(item);
        amount.openConfirmationGUI(player, tile);

        if (player.openContainer instanceof ContainerCraftConfirm) {
            ContainerCraftConfirm confirm = (ContainerCraftConfirm) player.openContainer;
            confirm.setItemToCraft(item);
            confirm.setAutoStart(message.isHeldShift());
        }
        return null;
    }
}
