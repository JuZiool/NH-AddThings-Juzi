package com.juzi.nhaddtingsjuzi.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ModNetworkClient {

    private static boolean registered;

    private ModNetworkClient() {}

    public static void register() {
        if (registered) {
            return;
        }
        ModNetwork.registerClient();
        registered = true;
    }
}
