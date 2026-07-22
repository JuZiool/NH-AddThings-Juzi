package com.juzi.nhaddtingsjuzi.terminal.client;

import java.util.LinkedList;
import java.util.List;

import com.juzi.nhaddtingsjuzi.network.ModNetwork;

import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** NEI recipe-overlay bridge for the mixed terminal's custom container type. */
public final class DualTerminalCraftingTransferHandler implements IOverlayHandler {
    public static final DualTerminalCraftingTransferHandler INSTANCE =
            new DualTerminalCraftingTransferHandler();

    private DualTerminalCraftingTransferHandler() {}

    @Override
    public void overlayRecipe(GuiContainer gui, IRecipeHandler recipeHandler,
            int recipeIndex, boolean shift) {
        try {
            NBTTagCompound ingredients = packIngredients(
                    gui, recipeHandler.getIngredientStacks(recipeIndex));
            ModNetwork.sendDualTerminalRecipe(ingredients);
        } catch (Exception ignored) {
            // Match AE2Things: a malformed third-party NEI recipe must not close the GUI.
        } catch (Error ignored) {
            // Some legacy recipe handlers throw linkage errors while enumerating variants.
        }
    }

    @SuppressWarnings("unchecked")
    private static NBTTagCompound packIngredients(GuiContainer gui,
            List<PositionedStack> positionedIngredients) {
        NBTTagCompound packed = new NBTTagCompound();
        if (positionedIngredients == null) return packed;

        for (PositionedStack positioned : positionedIngredients) {
            if (positioned == null || positioned.items == null
                    || positioned.items.length == 0) continue;

            int column = (positioned.relx - 25) / 18;
            int row = (positioned.rely - 6) / 18;
            int recipeSlot = column + row * 3;
            if (recipeSlot < 0 || recipeSlot >= 9) continue;

            for (Object object : gui.inventorySlots.inventorySlots) {
                if (!(object instanceof SlotCraftingMatrix)
                        && !(object instanceof SlotFakeCraftingMatrix)) continue;
                Slot slot = (Slot) object;
                if (slot.getSlotIndex() != recipeSlot) continue;

                List<ItemStack> ordered = new LinkedList<ItemStack>();
                for (ItemStack item : positioned.items) {
                    if (item == null) continue;
                    if (Platform.isRecipePrioritized(item)) ordered.add(0, item);
                    else ordered.add(item);
                }

                NBTTagList alternatives = new NBTTagList();
                for (ItemStack item : ordered) {
                    alternatives.appendTag(item.writeToNBT(new NBTTagCompound()));
                }
                packed.setTag("#" + slot.getSlotIndex(), alternatives);
                break;
            }
        }
        return packed;
    }
}
