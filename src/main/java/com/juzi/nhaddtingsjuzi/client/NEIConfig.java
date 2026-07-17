package com.juzi.nhaddtingsjuzi.client;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.registry.ModMachines;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import net.minecraft.item.ItemStack;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        ItemStack chargingStationStack = ModMachines.chargingStationStack.copy();
        API.addItemVariant(chargingStationStack.getItem(), chargingStationStack);
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
