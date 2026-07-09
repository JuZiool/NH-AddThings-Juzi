package com.juzi.nhaddtingsjuzi.recipe;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * 注册本模组的所有神秘时代4奥术合成配方
 */
public class RecipeArcane {

    public static void register() {
        // 基础要素消耗：六元素各 50
        AspectList aspects = new AspectList();
        aspects.add(Aspect.AIR, 50);
        aspects.add(Aspect.EARTH, 50);
        aspects.add(Aspect.FIRE, 50);
        aspects.add(Aspect.WATER, 50);
        aspects.add(Aspect.ORDER, 50);
        aspects.add(Aspect.ENTROPY, 50);

        // 配方原料
        // 源动之焰 → Thaumcraft Alumentum (ItemResource:0)
        // 闪耀之光 → Thaumcraft Nitor (ItemResource:1)
        // 魔力布匹 → Thaumcraft Enchanted Fabric (ItemResource:7)
        // 秩序指环 → ThaumicExploration Ring of Ordo (discountRing:4)
        // 风之魔晶 → Thaumcraft Aer Crystal Block (blockCrystal:0)
        // 邪术之眼 → Thaumcraft Eldritch Eye (ItemEldritchObject:0)

        ItemStack alumentum = new ItemStack(GameRegistry.findItem("Thaumcraft", "ItemResource"), 1, 0);
        ItemStack nitor = new ItemStack(GameRegistry.findItem("Thaumcraft", "ItemResource"), 1, 1);
        ItemStack fabric = new ItemStack(GameRegistry.findItem("Thaumcraft", "ItemResource"), 1, 7);
        ItemStack ringOfOrdo = new ItemStack(GameRegistry.findItem("ThaumicExploration", "discountRing"), 1, 4);
        ItemStack aerCrystal = new ItemStack(GameRegistry.findBlock("Thaumcraft", "blockCrystal"), 1, 0);
        ItemStack eldritchEye = new ItemStack(GameRegistry.findItem("Thaumcraft", "ItemEldritchObject"), 1, 0);

        // 飞行符咒
        ItemStack result = new ItemStack(
                GameRegistry.findItem("nh_addtings_juzi", "flight_charm"), 1);

        // 注册奥术合成配方
        ThaumcraftApi.addArcaneCraftingRecipe(
                "",              // 不需要研究解锁
                result,
                aspects,
                new Object[]{    // 3x3 配方
                    "BAB",
                    "CDC",
                    "EFE",
                    'A', alumentum,
                    'B', nitor,
                    'C', fabric,
                    'D', ringOfOrdo,
                    'E', aerCrystal,
                    'F', eldritchEye
                }
        );
    }
}
