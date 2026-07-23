package com.juzi.nhaddtingsjuzi.terminal.container;

import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.container.implementations.ContainerCraftingTerm;
import net.minecraft.entity.player.InventoryPlayer;

/** Server container for the dual terminal, based on AE2 crafting terminal. */
public final class ContainerJuziDualTerminal extends ContainerCraftingTerm {

    public ContainerJuziDualTerminal(InventoryPlayer inventory, PartDualTerminal terminal) {
        super(inventory, terminal);
    }

    public boolean canCraft() {
        return isPowered();
    }
}
