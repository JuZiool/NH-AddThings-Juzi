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
import org.lwjgl.input.Keyboard;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFlightCharm extends Item implements IBaubleExpanded {

    private static final int FOOD_COST_INTERVAL = 600;
    private static final int MIN_FOOD_LEVEL = 4;

    private static final String TAG_FLY_TIMER = "flyTimer";
    private static final String TAG_OWNS_FLIGHT = "ownsFlightPermission";
    private static final String TAG_FLIGHT_OWNER_UUID = "flightOwnerUuid";
    /** 每 30 秒通过 addExhaustion 消耗的疲劳度（8.0 = 1 个鸡腿） */
    private static final float EXHAUSTION_COST = 8.0F;

    /** 物品图标，由主类 TextureStitchEvent 注册后赋值 */
    public static IIcon icon;

    public ItemFlightCharm() {
        this.setUnlocalizedName("flight_charm");
        this.setMaxStackSize(1);
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
        updateFlightPermission(stack, ep);
        tickFlightConsumption(stack, ep);
    }

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {
        if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;
        updateFlightPermission(stack, (EntityPlayer) player);
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {
        if (player.worldObj.isRemote || !(player instanceof EntityPlayer)) return;
        releaseOwnedFlight(stack, (EntityPlayer) player);
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
        updateFlightPermission(stack, (EntityPlayer) player);
    }

    // ========== Tooltip ==========

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player,
                               List list, boolean advanced) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.GRAY + "佩戴后获得创造飞行能力");
            list.add(EnumChatFormatting.GRAY + "每累计飞行 30 秒消耗 1 格饱食度");
        } else {
            list.add(EnumChatFormatting.GRAY + "按住 Shift 查看详情");
        }
    }

    // ========== 内部逻辑 ==========

    private void updateFlightPermission(ItemStack stack, EntityPlayer ep) {
        NBTTagCompound tag = getOrCreateTag(stack);
        String playerUuid = ep.getUniqueID().toString();
        resetTransferredOwnership(tag, playerUuid);

        boolean ownsFlight = tag.getBoolean(TAG_OWNS_FLIGHT);
        boolean ownerMatches = playerUuid.equals(tag.getString(TAG_FLIGHT_OWNER_UUID));
        boolean creative = ep.capabilities.isCreativeMode;
        boolean hasEnoughFood = ep.getFoodStats().getFoodLevel() >= MIN_FOOD_LEVEL;

        if (creative) {
            tag.setBoolean(TAG_OWNS_FLIGHT, false);
            return;
        }

        if (!hasEnoughFood) {
            releaseOwnedFlight(stack, ep);
            return;
        }

        if (FlightCharmLogic.shouldClaimFlight(
                ownsFlight, ep.capabilities.allowFlying, false, true)) {
            tag.setBoolean(TAG_OWNS_FLIGHT, true);
            ep.capabilities.allowFlying = true;
            ep.sendPlayerAbilities();
        } else if (FlightCharmLogic.shouldRestoreOwnedFlight(
                ownsFlight, ownerMatches, ep.capabilities.allowFlying, false, true)) {
            ep.capabilities.allowFlying = true;
            ep.sendPlayerAbilities();
        }
    }

    private void releaseOwnedFlight(ItemStack stack, EntityPlayer ep) {
        NBTTagCompound tag = getOrCreateTag(stack);
        String playerUuid = ep.getUniqueID().toString();
        resetTransferredOwnership(tag, playerUuid);

        boolean ownsFlight = tag.getBoolean(TAG_OWNS_FLIGHT);
        boolean ownerMatches = playerUuid.equals(tag.getString(TAG_FLIGHT_OWNER_UUID));

        if (FlightCharmLogic.shouldReleaseOwnedFlight(
                ownsFlight, ownerMatches, ep.capabilities.isCreativeMode)) {
            ep.capabilities.allowFlying = false;
            ep.capabilities.isFlying = false;
            ep.sendPlayerAbilities();
        }
        tag.setBoolean(TAG_OWNS_FLIGHT, false);
    }

    private void tickFlightConsumption(ItemStack stack, EntityPlayer ep) {
        NBTTagCompound tag = getOrCreateTag(stack);
        boolean ownsFlight = tag.getBoolean(TAG_OWNS_FLIGHT);
        boolean ownerMatches = ep.getUniqueID().toString()
                .equals(tag.getString(TAG_FLIGHT_OWNER_UUID));
        boolean hasEnoughFood = ep.getFoodStats().getFoodLevel() >= MIN_FOOD_LEVEL;

        if (!FlightCharmLogic.shouldCountFlight(
                ownsFlight, ownerMatches, ep.capabilities.isFlying,
                ep.capabilities.isCreativeMode, hasEnoughFood)) {
            return;
        }

        int timer = tag.getInteger(TAG_FLY_TIMER);
        if (FlightCharmLogic.shouldChargeOnNextTick(timer, FOOD_COST_INTERVAL)) {
            ep.getFoodStats().addExhaustion(EXHAUSTION_COST);
        }
        tag.setInteger(TAG_FLY_TIMER,
                FlightCharmLogic.nextTimer(timer, FOOD_COST_INTERVAL));
    }

    private void resetTransferredOwnership(NBTTagCompound tag, String playerUuid) {
        if (!FlightCharmLogic.shouldResetFlightOwner(
                tag.getString(TAG_FLIGHT_OWNER_UUID), playerUuid)) {
            return;
        }

        tag.setBoolean(TAG_OWNS_FLIGHT, false);
        tag.setInteger(TAG_FLY_TIMER, 0);
        tag.setString(TAG_FLIGHT_OWNER_UUID, playerUuid);
    }

    private NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }
}
