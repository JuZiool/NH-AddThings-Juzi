package com.juzi.nhaddtingsjuzi.item;

import java.util.List;

import baubles.api.BaubleType;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFlightCharm extends Item implements IBaubleExpanded {

    private static final int FOOD_COST_INTERVAL = 600;
    private static final int MIN_FOOD_LEVEL = 4;

    private static final String TAG_FLY_TIMER = "flyTimer";
    /** 每 30 秒通过 addExhaustion 消耗的疲劳度（8.0 = 1 个鸡腿） */
    private static final float EXHAUSTION_COST = 8.0F;

    /** 物品图标，由 ClientEventHandler 在 TextureStitchEvent 中赋值 */
    public static IIcon icon;

    public ItemFlightCharm() {
        ((Item) this).setUnlocalizedName("flight_charm");
        ((Item) this).setMaxStackSize(1);
    }

    // ========== 纹理 ==========

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return icon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return icon;
    }

    // ========== IBaubleExpanded ==========

    @Override
    public String[] getBaubleTypes(ItemStack stack) {
        return new String[]{BaubleExpandedSlots.charmType};
    }

    // ========== IBauble ==========

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.UNIVERSAL;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;

        EntityPlayer ep = (EntityPlayer) player;
        updateFlightPermission(ep);
        tickFlightConsumption(stack, ep);
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {
        if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;
        updateFlightPermission((EntityPlayer) player);
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {
        if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;
        disableFlight((EntityPlayer) player);
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    public void onPlayerLoad(ItemStack stack, EntityLivingBase player) {
        if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;
        updateFlightPermission((EntityPlayer) player);
    }

    // ========== Tooltip ==========

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player,
                               List list, boolean advanced) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.GRAY
                    + StatCollector.translateToLocal("item.flight_charm.tooltip.detail1"));
            list.add(EnumChatFormatting.GRAY
                    + StatCollector.translateToLocal("item.flight_charm.tooltip.detail2"));
        } else {
            list.add(EnumChatFormatting.GRAY
                    + StatCollector.translateToLocal("item.flight_charm.tooltip.shift"));
        }
    }

    // ========== 内部逻辑 ==========

    private void updateFlightPermission(EntityPlayer ep) {
        boolean creative = ep.capabilities.isCreativeMode;
        boolean hasEnoughFood = ep.getFoodStats().getFoodLevel() >= MIN_FOOD_LEVEL;

        if (creative) return;

        if (!hasEnoughFood) {
            disableFlight(ep);
            return;
        }

        if (FlightCharmLogic.shouldEnableFlight(
                ep.capabilities.allowFlying, creative, hasEnoughFood)) {
            ep.capabilities.allowFlying = true;
            ep.sendPlayerAbilities();
        }
    }

    private void disableFlight(EntityPlayer ep) {
        if (FlightCharmLogic.shouldDisableFlight(
                ep.capabilities.allowFlying, ep.capabilities.isFlying,
                ep.capabilities.isCreativeMode)) {
            ep.capabilities.allowFlying = false;
            ep.capabilities.isFlying = false;
            ep.sendPlayerAbilities();
        }
    }

    private void tickFlightConsumption(ItemStack stack, EntityPlayer ep) {
        NBTTagCompound tag = getOrCreateTag(stack);
        boolean hasEnoughFood = ep.getFoodStats().getFoodLevel() >= MIN_FOOD_LEVEL;

        if (!FlightCharmLogic.shouldCountFlight(
                ep.capabilities.isFlying, ep.capabilities.isCreativeMode,
                hasEnoughFood)) {
            return;
        }

        int timer = tag.getInteger(TAG_FLY_TIMER);
        if (FlightCharmLogic.shouldChargeOnNextTick(timer, FOOD_COST_INTERVAL)) {
            ep.getFoodStats().addExhaustion(EXHAUSTION_COST);
        }
        tag.setInteger(TAG_FLY_TIMER,
                FlightCharmLogic.nextTimer(timer, FOOD_COST_INTERVAL));
    }

    private NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }
}
