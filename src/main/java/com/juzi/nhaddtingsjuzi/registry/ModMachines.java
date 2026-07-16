package com.juzi.nhaddtingsjuzi.registry;

import com.juzi.nhaddtingsjuzi.machine.MTEChargingStation;
import com.juzi.nhaddtingsjuzi.machine.ChargingStationTextureSpec;

import gregtech.api.GregTechAPI;
import net.minecraft.item.ItemStack;

public final class ModMachines {

    public static final int CHARGING_STATION_META_ID = 31990;
    public static MTEChargingStation chargingStation;
    public static ItemStack chargingStationStack;

    private ModMachines() {}

    public static void register() {
        chargingStation = new MTEChargingStation(
                CHARGING_STATION_META_ID,
                "charging_station",
                "Charging Station",
                ChargingStationTextureSpec.MACHINE_TIER);
        chargingStationStack = chargingStation.getStackForm(1L);
        System.out.println("[NH-AddTings-Juzi] Registered Charging Station MTE "
                + CHARGING_STATION_META_ID + ": "
                + (GregTechAPI.METATILEENTITIES[CHARGING_STATION_META_ID] == chargingStation));
    }
}
