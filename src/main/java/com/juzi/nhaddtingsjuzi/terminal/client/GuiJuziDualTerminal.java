package com.juzi.nhaddtingsjuzi.terminal.client;

import java.util.List;

import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerJuziDualTerminal;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

/** Original AE2Things crafting-terminal GUI, fed by both AE storage channels. */
@SideOnly(Side.CLIENT)
public final class GuiJuziDualTerminal extends GuiCraftingTerminal {
    public GuiJuziDualTerminal(InventoryPlayer inventory, PartDualTerminal terminal) {
        this(new ContainerJuziDualTerminal(inventory, terminal));
    }

    private GuiJuziDualTerminal(ContainerJuziDualTerminal container) {
        // Use the already compiled concrete AE2Things GUI implementation. This
        // is important because AEBaseGui's background bridge is final at
        // runtime; re-declaring it in this mod causes IncompatibleClassChangeError.
        super((Container) container);
        xSize = 195;
        ySize = 204;
        standardSize = xSize;
        reservedSpace = 73;
        container.setGui(this);
        // Keep AE2Things' native repository ordering so item and fluid
        // display entries participate in one shared sort instead of being
        // partitioned into separate item-first/fluid-last groups.
    }

    @Override
    public void postFluidUpdate(List<IAEFluidStack> fluids) {
        for (IAEFluidStack fluid : fluids) {
            if (fluid == null || fluid.getFluidStack() == null) continue;
            ItemStack display = ItemFluidDrop.newDisplayStack(fluid.getFluidStack());
            IAEItemStack entry = AEItemStack.create(display);
            if (entry == null) continue;
            entry.setStackSize(fluid.getStackSize());
            entry.setCraftable(fluid.isCraftable());
            repo.postUpdate(entry);
        }
        repo.updateView();
        setScrollBar();
    }

    @Override
    public void postUpdate(List<IAEItemStack> items) {
        for (IAEItemStack item : items) {
            if (item == null) continue;
            if (item.getItem() instanceof ItemFluidDrop) {
                ItemStack display = ItemFluidDrop.newDisplayStack(
                        ItemFluidDrop.getFluidStack(item.getItemStack()));
                IAEItemStack entry = AEItemStack.create(display);
                if (entry == null) continue;
                entry.setStackSize(item.getStackSize());
                entry.setCraftable(item.isCraftable());
                repo.postUpdate(entry);
            } else {
                repo.postUpdate(item);
            }
        }
        repo.updateView();
        setScrollBar();
    }
}
