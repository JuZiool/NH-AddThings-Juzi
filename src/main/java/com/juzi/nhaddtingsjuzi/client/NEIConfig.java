package com.juzi.nhaddtingsjuzi.client;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.registry.ModMachines;
import com.juzi.nhaddtingsjuzi.terminal.client.DualTerminalCraftingTransferHandler;
import com.juzi.nhaddtingsjuzi.terminal.client.GuiJuziDualTerminal;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.api.INEIGuiAdapter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class NEIConfig implements IConfigureNEI {

    private static final INEIGuiAdapter DUAL_TERMINAL_GUI_HANDLER = new INEIGuiAdapter() {
        @Override
        public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int width, int height) {
            return gui instanceof GuiJuziDualTerminal
                    && ((GuiJuziDualTerminal) gui).hideItemPanelSlot(x, y, width, height);
        }
    };

    @Override
    public void loadConfig() {
        ItemStack chargingStationStack = ModMachines.chargingStationStack.copy();
        API.addItemVariant(chargingStationStack.getItem(), chargingStationStack);
        API.registerNEIGuiHandler(DUAL_TERMINAL_GUI_HANDLER);
        API.registerGuiOverlayHandler(
                GuiJuziDualTerminal.class,
                DualTerminalCraftingTransferHandler.INSTANCE,
                "crafting");
        API.registerGuiOverlayHandler(
                GuiJuziDualTerminal.class,
                DualTerminalCraftingTransferHandler.INSTANCE,
                "crafting2x2");
        System.out.println("[NH-AddTings-Juzi] Added Charging Station to NEI item list");
    }

    @Override
    public String getName() {
        return NHAddTingsJuzi.NAME;
    }

    @Override
    public String getVersion() {
        return NHAddTingsJuzi.VERSION;
    }
}
