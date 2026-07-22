package com.juzi.nhaddtingsjuzi.registry;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.item.ItemFlightCharm;
import com.juzi.nhaddtingsjuzi.item.ItemTieredVajra;
import com.juzi.nhaddtingsjuzi.item.ItemUnrestrictedShell;
import com.juzi.nhaddtingsjuzi.storage.UnrestrictedCellHandler;
import com.juzi.nhaddtingsjuzi.storage.UnrestrictedCellItem;
import com.juzi.nhaddtingsjuzi.storage.UnrestrictedFluidCellItem;
import com.juzi.nhaddtingsjuzi.terminal.item.ItemPartDualTerminal;

import appeng.api.AEApi;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ToolDictNames;
import gregtech.api.objects.GTItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class ModItems {

    public static final String FLIGHT_CHARM_ID = "flight_charm";
    public static final String HV_VAJRA_ID = "hv_vajra";
    public static ItemFlightCharm flightCharm;
    public static ItemTieredVajra hvVajra;
    public static ItemUnrestrictedShell unrestrictedShell;
    public static UnrestrictedCellItem itemCell1k;
    public static UnrestrictedCellItem itemCell4k;
    public static UnrestrictedCellItem itemCell16k;
    public static UnrestrictedCellItem itemCell64k;
    public static UnrestrictedFluidCellItem fluidCell1k;
    public static UnrestrictedFluidCellItem fluidCell4k;
    public static UnrestrictedFluidCellItem fluidCell16k;
    public static UnrestrictedFluidCellItem fluidCell64k;
    public static ItemPartDualTerminal dualTerminal;

    private ModItems() {}

    public static void register() {
        flightCharm = new ItemFlightCharm();
        flightCharm.setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
        GameRegistry.registerItem(flightCharm, FLIGHT_CHARM_ID);

        hvVajra = ItemTieredVajra.createHv(HV_VAJRA_ID);
        hvVajra.setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
        GameRegistry.registerItem(hvVajra, HV_VAJRA_ID);
        registerVajraToolIdentities();
        unrestrictedShell = new ItemUnrestrictedShell();
        GameRegistry.registerItem(unrestrictedShell, "unrestricted_shell");
        itemCell1k = UnrestrictedCellItem.create("unrestricted_item_cell_1k", 1024);
        itemCell4k = UnrestrictedCellItem.create("unrestricted_item_cell_4k", 4096);
        itemCell16k = UnrestrictedCellItem.create("unrestricted_item_cell_16k", 16384);
        itemCell64k = UnrestrictedCellItem.create("unrestricted_item_cell_64k", 65536);
        fluidCell1k = UnrestrictedFluidCellItem.create("unrestricted_fluid_cell_1k", 1024);
        fluidCell4k = UnrestrictedFluidCellItem.create("unrestricted_fluid_cell_4k", 4096);
        fluidCell16k = UnrestrictedFluidCellItem.create("unrestricted_fluid_cell_16k", 16384);
        fluidCell64k = UnrestrictedFluidCellItem.create("unrestricted_fluid_cell_64k", 65536);
        dualTerminal = new ItemPartDualTerminal().register();
        AEApi.instance().registries().cell().addCellHandler(new UnrestrictedCellHandler());
    }

    private static void registerVajraToolIdentities() {
        ItemStack stack = new ItemStack(hvVajra, 1, OreDictionary.WILDCARD_VALUE);
        GTItemStack toolKey = new GTItemStack(stack);
        VajraSpecialToolRegistration.register(
                toolKey,
                GregTechAPI.sToolList,
                GregTechAPI.sWrenchList,
                GregTechAPI.sWireCutterList);
        OreDictionary.registerOre(ToolDictNames.craftingToolPickaxe.name(), stack);
        OreDictionary.registerOre(ToolDictNames.craftingToolShovel.name(), stack);
        OreDictionary.registerOre(ToolDictNames.craftingToolAxe.name(), stack);
        OreDictionary.registerOre(ToolDictNames.craftingToolWrench.name(), stack);
        OreDictionary.registerOre(ToolDictNames.craftingToolWireCutter.name(), stack);
    }

}
