package com.juzi.nhaddtingsjuzi.network;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.terminal.network.DualTerminalGuiHandler;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

/** Reopens the custom terminal after native AE2 crafting-status navigation. */
public final class DualTerminalReturnHandler
        implements IMessageHandler<DualTerminalReturnMessage, IMessage> {

    @Override
    public IMessage onMessage(DualTerminalReturnMessage message, MessageContext context) {
        EntityPlayerMP player = context.getServerHandler().playerEntity;
        if (player == null || !(player.openContainer instanceof AEBaseContainer)) return null;

        AEBaseContainer container = (AEBaseContainer) player.openContainer;
        if (!(container.getTarget() instanceof PartDualTerminal)) return null;

        ContainerOpenContext openContext = container.getOpenContext();
        TileEntity tile = openContext == null ? null : openContext.getTile();
        if (tile == null || openContext.getSide() == null
                || DualTerminalGuiHandler.findPart(tile, openContext.getSide()) == null) {
            return null;
        }

        FMLNetworkHandler.openGui(player, NHAddTingsJuzi.INSTANCE,
                DualTerminalGuiHandler.guiId(openContext.getSide()), tile.getWorldObj(),
                tile.xCoord, tile.yCoord, tile.zCoord);
        return null;
    }
}
