package com.juzi.nhaddtingsjuzi.registry;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.item.ItemFlightCharm;

import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {

    public static final String FLIGHT_CHARM_ID = "flight_charm";
    public static ItemFlightCharm flightCharm;

    private ModItems() {}

    public static void register() {
        flightCharm = new ItemFlightCharm();
        flightCharm.setCreativeTab(NHAddTingsJuzi.tabNHAddTings);
        GameRegistry.registerItem(flightCharm, FLIGHT_CHARM_ID);
    }
}
