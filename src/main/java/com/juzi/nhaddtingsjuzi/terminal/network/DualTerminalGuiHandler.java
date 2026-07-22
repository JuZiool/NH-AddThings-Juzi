package com.juzi.nhaddtingsjuzi.terminal.network;

import java.lang.reflect.Constructor;

import com.asdflj.ae2thing.client.gui.container.ContainerJuziDualTerminal;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.api.parts.IPartHost;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** Opens the interactive mixed item/fluid crafting terminal. */
public final class DualTerminalGuiHandler implements IGuiHandler {
    private static final int GUI_BASE = 0x4E40;
    public static final DualTerminalGuiHandler INSTANCE = new DualTerminalGuiHandler();
    private static final String CLIENT_GUI_CLASS =
            "com.juzi.nhaddtingsjuzi.terminal.client.GuiJuziDualTerminal";

    private DualTerminalGuiHandler() {}

    public static int guiId(ForgeDirection side) {
        int sideOrdinal = side == null ? ForgeDirection.UNKNOWN.ordinal() : side.ordinal();
        return GUI_BASE + sideOrdinal;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world,
            int x, int y, int z) {
        ForgeDirection side = decodeSide(id);
        if (side == null || world == null || player == null) return null;
        TileEntity tile = world.getTileEntity(x, y, z);
        PartDualTerminal part = findPart(tile, side);
        if (part == null) return null;
        Container container = new ContainerJuziDualTerminal(player.inventory, part);
        setOpenContext(container, part, world, x, y, z, side);
        return container;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world,
            int x, int y, int z) {
        ForgeDirection side = decodeSide(id);
        if (side == null || world == null || player == null) return null;
        PartDualTerminal part = findPart(world.getTileEntity(x, y, z), side);
        if (part == null) return null;
        try {
            Class<?> guiClass = Class.forName(CLIENT_GUI_CLASS);
            Constructor<?> constructor = guiClass.getConstructor(
                    net.minecraft.entity.player.InventoryPlayer.class, PartDualTerminal.class);
            return constructor.newInstance(player.inventory, part);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static PartDualTerminal findPart(TileEntity tile, ForgeDirection side) {
        if (!(tile instanceof IPartHost) || side == null) return null;
        Object part = ((IPartHost) tile).getPart(side);
        return part instanceof PartDualTerminal ? (PartDualTerminal) part : null;
    }

    private static void setOpenContext(Container container, PartDualTerminal part,
            World world, int x, int y, int z, ForgeDirection side) {
        if (!(container instanceof AEBaseContainer)) return;
        AEBaseContainer aeContainer = (AEBaseContainer) container;
        ContainerOpenContext openContext = new ContainerOpenContext(part);
        openContext.setWorld(world);
        openContext.setX(x);
        openContext.setY(y);
        openContext.setZ(z);
        openContext.setSide(side);
        aeContainer.setOpenContext(openContext);
    }

    private static ForgeDirection decodeSide(int id) {
        int sideOrdinal = id - GUI_BASE;
        if (sideOrdinal < 0 || sideOrdinal >= ForgeDirection.VALID_DIRECTIONS.length) return null;
        return ForgeDirection.getOrientation(sideOrdinal);
    }
}
