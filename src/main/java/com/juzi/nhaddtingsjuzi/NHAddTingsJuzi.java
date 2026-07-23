package com.juzi.nhaddtingsjuzi;

import com.juzi.nhaddtingsjuzi.client.ClientEventHandler;
import com.juzi.nhaddtingsjuzi.network.ModNetwork;
import com.juzi.nhaddtingsjuzi.network.ModNetworkClient;
import com.juzi.nhaddtingsjuzi.terminal.network.DualTerminalGuiHandler;

import cpw.mods.fml.common.network.NetworkRegistry;
import com.juzi.nhaddtingsjuzi.registry.ModItems;
import com.juzi.nhaddtingsjuzi.registry.ModMachines;
import com.juzi.nhaddtingsjuzi.registry.ModRecipes;
import com.juzi.nhaddtingsjuzi.registry.CreativeTabEntries;

import java.util.List;

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
        dependencies = "required-after:gregtech;required-after:IC2;"
                + "required-after:appliedenergistics2;required-after:ae2fc")
public class NHAddTingsJuzi
{
    @Mod.Instance(NHAddTingsJuzi.MODID)
    public static NHAddTingsJuzi INSTANCE;

    public static final String MODID = "nh_addtings_juzi";
    public static final String NAME = "NH-AddTings-Juzi";
    public static final String VERSION = "0.1.9b";

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

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public void displayAllReleventItems(List entries) {
            // Items already on this tab (charm, vajra, shell, cells) come from super.
            // GT machines are not normal items, so only the charging station is appended.
            super.displayAllReleventItems(entries);
            CreativeTabEntries.append(entries, ModMachines.chargingStationStack);
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModNetwork.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, DualTerminalGuiHandler.INSTANCE);
        if (event.getSide() == Side.CLIENT) {
            ModNetworkClient.register();
        }
        // 注册飞行符咒物品
        ModItems.register();
        ModMachines.register();

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
