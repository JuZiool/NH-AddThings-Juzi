package com.juzi.nhaddtingsjuzi.client;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.item.ItemFlightCharm;
import com.juzi.nhaddtingsjuzi.item.ItemTieredVajra;
import com.juzi.nhaddtingsjuzi.registry.ModItems;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientEventHandler {

    private ClientEventHandler() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 1) {
            ItemFlightCharm.icon = event.map.registerIcon(
                    NHAddTingsJuzi.MODID + ":" + ModItems.FLIGHT_CHARM_ID);
            ItemTieredVajra.icon = event.map.registerIcon(
                    NHAddTingsJuzi.MODID + ":" + ModItems.HV_VAJRA_ID);
        }
    }
}
