package com.juzi.nhaddtingsjuzi.terminal.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

/** Carries an NEI crafting-grid overlay from the mixed terminal to the server. */
public final class DualTerminalRecipeMessage implements IMessage {
    private NBTTagCompound ingredients;

    public DualTerminalRecipeMessage() {}

    public DualTerminalRecipeMessage(NBTTagCompound ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        ingredients = ByteBufUtils.readTag(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeTag(buffer, ingredients);
    }

    NBTTagCompound getIngredients() {
        return ingredients;
    }
}
