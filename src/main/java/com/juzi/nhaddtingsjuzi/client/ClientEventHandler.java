package com.juzi.nhaddtingsjuzi.client;

import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.item.ItemFlightCharm;
import com.juzi.nhaddtingsjuzi.item.ItemTieredVajra;
import com.juzi.nhaddtingsjuzi.item.VajraLogic;
import com.juzi.nhaddtingsjuzi.machine.MTEChargingStation;
import com.juzi.nhaddtingsjuzi.registry.ModItems;
import com.juzi.nhaddtingsjuzi.network.ModNetwork;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;

import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerCraftingStatus;

import gregtech.api.util.GTUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
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
        if (event.map.getTextureType() == 0) {
            MTEChargingStation.FRONT_OVERLAY.register(event.map);
        }
        if (event.map.getTextureType() == 1) {
            ItemFlightCharm.icon = event.map.registerIcon(
                    NHAddTingsJuzi.MODID + ":" + ModItems.FLIGHT_CHARM_ID);
            ItemTieredVajra.icon = event.map.registerIcon(
                    NHAddTingsJuzi.MODID + ":" + ModItems.HV_VAJRA_ID);
        }
    }

    @SubscribeEvent
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (!(event.gui instanceof GuiCraftingStatus)
                || !(event.button instanceof GuiTabButton)) {
            return;
        }

        GuiContainer gui = (GuiContainer) event.gui;
        if (gui.inventorySlots instanceof ContainerCraftingStatus
                && gui.inventorySlots instanceof appeng.container.AEBaseContainer
                && ((appeng.container.AEBaseContainer) gui.inventorySlots).getTarget()
                        instanceof PartDualTerminal) {
            event.setCanceled(true);
            ModNetwork.sendDualTerminalReturnRequest();
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack heldItem = player == null ? null : player.getHeldItem();
        boolean holdingVajra = heldItem != null
                && heldItem.getItem() == ModItems.hvVajra;
        String message = event.message.getUnformattedText();
        boolean gregTechToolMessage = message.equals(GTUtility.trans("212", "Input enabled"))
                || message.equals(GTUtility.trans("213", "Input disabled"))
                || message.equals(GTUtility.trans("214", "Connected"))
                || message.equals(GTUtility.trans("215", "Disconnected"));
        if (VajraLogic.shouldSuppressToolMessage(holdingVajra, gregTechToolMessage)) {
            event.setCanceled(true);
        }
    }
}
