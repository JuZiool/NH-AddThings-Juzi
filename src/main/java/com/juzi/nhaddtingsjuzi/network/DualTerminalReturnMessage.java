package com.juzi.nhaddtingsjuzi.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/** Requests returning from the native crafting-status screen to the dual terminal. */
public final class DualTerminalReturnMessage implements IMessage {
    @Override
    public void fromBytes(ByteBuf buffer) {
        // No payload.
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        // No payload.
    }
}
