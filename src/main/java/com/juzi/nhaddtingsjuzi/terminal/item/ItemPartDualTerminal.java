package com.juzi.nhaddtingsjuzi.terminal.item;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import com.juzi.nhaddtingsjuzi.NHAddTingsJuzi;
import com.juzi.nhaddtingsjuzi.terminal.parts.PartDualTerminal;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

/** Item form of the dual item/fluid AE cable-bus terminal. */
public class ItemPartDualTerminal extends Item implements IPartItem {

    public static final String ID = "dual_terminal";

    public ItemPartDualTerminal() {
        setMaxStackSize(64);
        setUnlocalizedName(ID);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Override
    public IPart createPartFromItemStack(ItemStack stack) {
        return new PartDualTerminal(stack);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                              int x, int y, int z, int side,
                              float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(
                player.getHeldItem(), x, y, z, side, player, world);
    }


    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(StatCollector.translateToLocal("nh_addtings_juzi.dual_terminal.item_mode"));
        tooltip.add(StatCollector.translateToLocal("nh_addtings_juzi.dual_terminal.fluid_mode"));
    }

    /** Registers the part item after the mod's common item bootstrap. */
    public ItemPartDualTerminal register() {
        GameRegistry.registerItem(this, ID);
        setCreativeTab(NHAddTingsJuzi.TAB_NH_ADD_TINGS);
        return this;
    }

    /** The cable-bus renderer supplies the icon; no standalone atlas icon is needed. */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {}

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }
}
