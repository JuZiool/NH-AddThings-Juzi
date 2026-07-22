package com.juzi.nhaddtingsjuzi.terminal.parts;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.util.DimensionalCoord;
import appeng.client.texture.CableBusTextures;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.util.Platform;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.terminal.network.DualTerminalGuiHandler;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

/**
 * AE cable-bus terminal with one interactive mixed item/fluid crafting screen.
 * Items and AE2FC fluid display entries share the native AE2Things terminal
 * repository, sorting, search and click handling.
 */
public class PartDualTerminal extends PartCraftingTerminal {

    private static final CableBusTextures FRONT_BRIGHT_ICON = CableBusTextures.PartTerminal_Bright;
    private static final CableBusTextures FRONT_COLORED_ICON = CableBusTextures.PartTerminal_Colored;
    private static final CableBusTextures FRONT_DARK_ICON = CableBusTextures.PartTerminal_Dark;

    public PartDualTerminal(ItemStack stack) {
        super(stack);
    }

    @Override
    public CableBusTextures getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public CableBusTextures getFrontColored() {
        return FRONT_COLORED_ICON;
    }

    @Override
    public CableBusTextures getFrontDark() {
        return FRONT_DARK_ICON;
    }

    @Override
    public boolean isLightSource() {
        return false;
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, Vec3 hitVec) {
        if (player == null) return false;
        TileEntity tile = getTile();

        // Preserve AE2's normal wrench rotation/removal handling.
        if (tile != null && Platform.isWrench(player, player.getHeldItem(),
                tile.xCoord, tile.yCoord, tile.zCoord)) {
            return super.onPartActivate(player, hitVec);
        }

        if (Platform.isClient()) return true;
        if (tile == null || getHost() == null || getHost().getPart(getSide()) != this) {
            return false;
        }
        if (!canPlayerOpen(player, tile)) {
            return false;
        }

        FMLNetworkHandler.openGui(player, NHAddTingsJuzi.INSTANCE,
                DualTerminalGuiHandler.guiId(getSide()), tile.getWorldObj(),
                tile.xCoord, tile.yCoord, tile.zCoord);
        return true;
    }

    /**
     * Mirror AE GuiBridge open checks: block interaction + CRAFT security
     * (same required permission as GUI_CRAFTING_TERMINAL).
     */
    private boolean canPlayerOpen(EntityPlayer player, TileEntity tile) {
        if (!Platform.hasPermissions(new DimensionalCoord(tile), player)) {
            return false;
        }
        IGridNode node = getActionableNode();
        if (node == null) {
            return false;
        }
        IGrid grid = node.getGrid();
        if (grid == null) {
            return false;
        }
        ISecurityGrid security = grid.getCache(ISecurityGrid.class);
        return security != null && security.hasPermission(player, SecurityPermissions.CRAFT);
    }
}
