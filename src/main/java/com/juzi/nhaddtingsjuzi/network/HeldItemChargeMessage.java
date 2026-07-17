package com.juzi.nhaddtingsjuzi.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class HeldItemChargeMessage implements IMessage {

    private int inventorySlot;
    private int itemId;
    private double chargeAmount;

    public HeldItemChargeMessage() {}

    HeldItemChargeMessage(int inventorySlot, ItemStack stack, double chargeAmount) {
        this.inventorySlot = inventorySlot;
        itemId = Item.getIdFromItem(stack.getItem());
        this.chargeAmount = chargeAmount;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        inventorySlot = buffer.readUnsignedByte();
        itemId = buffer.readInt();
        chargeAmount = buffer.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(inventorySlot);
        buffer.writeInt(itemId);
        buffer.writeDouble(chargeAmount);
    }

    public int getInventorySlot() {
        return inventorySlot;
    }

    public int getItemId() {
        return itemId;
    }

    public double getChargeAmount() {
        return chargeAmount;
    }
}
