package com.juzi.nhaddtingsjuzi.registry;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.item.ItemFlightCharm;
import com.juzi.nhaddtingsjuzi.item.ItemTieredVajra;

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

    private ModItems() {}

    public static void register() {
        flightCharm = new ItemFlightCharm();
        flightCharm.setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
        GameRegistry.registerItem(flightCharm, FLIGHT_CHARM_ID);

        hvVajra = ItemTieredVajra.createHv(HV_VAJRA_ID);
        hvVajra.setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
        GameRegistry.registerItem(hvVajra, HV_VAJRA_ID);
        registerVajraToolIdentities();
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
