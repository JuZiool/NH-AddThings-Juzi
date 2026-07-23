package com.juzi.nhaddtingsjuzi.terminal.client;

import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.core.localization.GuiText;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

/**
 * Dual terminal GUI on AE2 2.9 native crafting terminal.
 * Item+fluid mixed listing relies on AE2 native fluid stack type + monitor updates.
 */
@SideOnly(Side.CLIENT)
public final class GuiJuziDualTerminal extends GuiCraftingTerm {

    public GuiJuziDualTerminal(InventoryPlayer inventory, PartDualTerminal terminal) {
        super(inventory, terminal);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        String title = StatCollector.translateToLocal("nh_addtings_juzi.dual_terminal.title");
        if (title == null || title.startsWith("nh_addtings_juzi.")) {
            title = GuiText.Terminal.getLocal();
        }
        this.fontRendererObj.drawString(this.getGuiDisplayName(title), 8, 6, 4210752);
    }
}
