package com.juzi.nhaddtingsjuzi.terminal.client;

import java.util.Arrays;
import java.util.List;

import com.asdflj.ae2thing.client.gui.GuiCraftingTerminal;
import com.asdflj.ae2thing.client.gui.container.ContainerJuziDualTerminal;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.localization.GuiColors;
import appeng.core.localization.GuiText;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

/** Original AE2Things crafting-terminal GUI, fed by both AE storage channels. */
@SideOnly(Side.CLIENT)
public final class GuiJuziDualTerminal extends GuiCraftingTerminal {
    private ItemStack[] currentViewCells = new ItemStack[5];

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
        // Enable view-cell UI affordances (craft-status tab placement etc.).
        viewCell = true;
        container.setGui(this);
        // Keep AE2Things' native repository ordering so item and fluid
        // display entries participate in one shared sort instead of being
        // partitioned into separate item-first/fluid-last groups.
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        // Middle label: crafting section (same place as vanilla Crafting Terminal).
        this.fontRendererObj.drawString(
                GuiText.CraftingTerminal.getLocal(),
                8,
                this.ySize - 96 + 1 - this.getReservedSpace(),
                GuiColors.CraftingTerminalTitle.getColor());
        // Top title: dual terminal name (replaces generic "Terminal").
        String title = StatCollector.translateToLocal("nh_addtings_juzi.dual_terminal.title");
        if (title == null || title.startsWith("nh_addtings_juzi.")) {
            title = GuiText.Terminal.getLocal();
        }
        this.fontRendererObj.drawString(this.getGuiDisplayName(title), 8, 6, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        applyViewCells();
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    private void applyViewCells() {
        if (!(inventorySlots instanceof ContainerJuziDualTerminal)) return;
        ItemStack[] cells = ((ContainerJuziDualTerminal) inventorySlots).getViewCells();
        if (Arrays.equals(currentViewCells, cells)) return;
        currentViewCells = cells == null ? new ItemStack[5] : cells.clone();
        repo.setViewCell(currentViewCells);
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
