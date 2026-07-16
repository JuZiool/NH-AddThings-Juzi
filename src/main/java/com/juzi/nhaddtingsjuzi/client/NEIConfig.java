package com.juzi.nhaddtingsjuzi.client;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.registry.ModMachines;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.addItemListEntry(ModMachines.chargingStationStack.copy());
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
