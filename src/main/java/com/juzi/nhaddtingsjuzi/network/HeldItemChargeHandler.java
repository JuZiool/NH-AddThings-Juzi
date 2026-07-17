package com.juzi.nhaddtingsjuzi.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.item.ElectricItem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@SideOnly(Side.CLIENT)
public final class HeldItemChargeHandler
        implements IMessageHandler<HeldItemChargeMessage, IMessage> {

    @Override
    public IMessage onMessage(final HeldItemChargeMessage message,
                               MessageContext context) {
        Minecraft.getMinecraft().func_152344_a(new Runnable() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft().thePlayer == null) {
                    return;
                }
                ItemStack held = Minecraft.getMinecraft().thePlayer.inventory
                        .getStackInSlot(message.getInventorySlot());
                if (held == null
                        || Item.getIdFromItem(held.getItem()) != message.getItemId()) {
                    return;
                }
                ElectricItem.manager.charge(
                        held,
                        message.getChargeAmount(),
                        Integer.MAX_VALUE,
                        true,
                        false);
            }
        });
        return null;
    }
}
