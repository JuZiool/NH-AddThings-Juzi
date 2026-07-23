package com.juzi.nhaddtingsjuzi.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/** Requests the amount dialog to continue crafting from a dual terminal. */
public final class DualTerminalCraftRequestMessage implements IMessage {
    private int amount;
    private boolean heldShift;

    public DualTerminalCraftRequestMessage() {}

    public DualTerminalCraftRequestMessage(int amount, boolean heldShift) {
        this.amount = amount;
        this.heldShift = heldShift;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isHeldShift() {
        return heldShift;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        amount = buffer.readInt();
        heldShift = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(amount);
        buffer.writeBoolean(heldShift);
    }
}
