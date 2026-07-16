package com.juzi.nhaddtingsjuzi;

import com.juzi.nhaddtingsjuzi.client.ClientEventHandler;
import com.juzi.nhaddtingsjuzi.registry.ModItems;
import com.juzi.nhaddtingsjuzi.registry.ModRecipes;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(
        modid = NHAddTingsJuzi.MODID,
        name = NHAddTingsJuzi.NAME,
        version = NHAddTingsJuzi.VERSION,
        dependencies = "required-after:gregtech;required-after:IC2")
public class NHAddTingsJuzi
{
    public static final String MODID = "nh_addtings_juzi";
    public static final String NAME = "NH-AddTings-Juzi";
    public static final String VERSION = "0.1.1b";

    /** 本模组专属创造模式标签页 */
    public static CreativeTabs TAB_NH_ADD_TINGS = new CreativeTabs("nh_addtings_juzi") {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return ModItems.flightCharm != null ? ModItems.flightCharm : net.minecraft.init.Items.feather;
        }
        @Override
        @SideOnly(Side.CLIENT)
        public String getTranslatedTabLabel() {
            return "NH-AddTings-Juzi";
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // 注册飞行符咒物品
        ModItems.register();

        // 注册纹理事件（客户端专用）
        if (event.getSide() == Side.CLIENT) {
            ClientEventHandler.register();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // 注册奥术合成配方
        ModRecipes.register();
        System.out.println(NAME + " v" + VERSION + " loaded!");
    }
}
