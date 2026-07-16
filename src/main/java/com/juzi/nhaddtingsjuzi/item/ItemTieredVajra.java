package com.juzi.nhaddtingsjuzi.item;

import java.util.List;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.util.GTToolHarvestHelper;

public class ItemTieredVajra extends Item implements IElectricItem {

    private final VajraTier tier;

    @SideOnly(Side.CLIENT)
    public static IIcon icon;

    private ItemTieredVajra(String unlocalizedName, VajraTier tier) {
        this.tier = tier;
        setUnlocalizedName(unlocalizedName);
        setMaxStackSize(1);
        setNoRepair();
    }

    public static ItemTieredVajra createHv(String unlocalizedName) {
        return new ItemTieredVajra(unlocalizedName, VajraTier.HV);
    }

    public int getOperationCost() {
        return tier.getOperationCost();
    }

    public ItemStack createStackWithCharge(double charge) {
        ItemStack stack = new ItemStack(this);
        ElectricItem.manager.charge(
                stack,
                VajraLogic.transferredCharge(charge, tier),
                tier.getElectricTier(),
                true,
                false);
        return stack;
    }

    private double getCharge(ItemStack stack) {
        return ElectricItem.manager.getCharge(stack);
    }

    private boolean hasOperationEnergy(ItemStack stack) {
        return VajraLogic.hasOperationEnergy(getCharge(stack), tier.getOperationCost());
    }

    @Override
    public boolean canProvideEnergy(ItemStack stack) {
        return false;
    }

    @Override
    public Item getChargedItem(ItemStack stack) {
        return this;
    }

    @Override
    public Item getEmptyItem(ItemStack stack) {
        return this;
    }

    @Override
    public double getMaxCharge(ItemStack stack) {
        return tier.getMaxCharge();
    }

    @Override
    public int getTier(ItemStack stack) {
        return tier.getElectricTier();
    }

    @Override
    public double getTransferLimit(ItemStack stack) {
        return tier.getTransferLimit();
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass) {
        return tier.getHarvestLevel();
    }

    @Override
    public boolean canHarvestBlock(Block block, ItemStack stack) {
        return hasOperationEnergy(stack);
    }

    @Override
    public float getDigSpeed(ItemStack stack, Block block, int metadata) {
        if (!hasOperationEnergy(stack) || !isMineableBlock(block, metadata)) {
            return 0.0F;
        }
        return tier.getMiningSpeed();
    }

    private boolean isMineableBlock(Block block, int metadata) {
        return GTToolHarvestHelper.isAppropriateTool(
                block, metadata, "pickaxe", "shovel", "axe")
                || GTToolHarvestHelper.isAppropriateMaterial(
                        block,
                        Material.rock,
                        Material.iron,
                        Material.anvil,
                        Material.ground,
                        Material.grass,
                        Material.sand,
                        Material.snow,
                        Material.craftedSnow,
                        Material.clay,
                        Material.wood,
                        Material.plants,
                        Material.vine,
                        Material.leaves,
                        Material.gourd);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block,
                                    int x, int y, int z, EntityLivingBase entity) {
        if (!world.isRemote && block.getBlockHardness(world, x, y, z) >= 0.0F
                && (!(entity instanceof EntityPlayer)
                || !((EntityPlayer) entity).capabilities.isCreativeMode)) {
            ElectricItem.manager.use(stack, tier.getOperationCost(), entity);
        }
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!player.isSneaking()) {
            return stack;
        }

        if (!world.isRemote) {
            boolean enabled = EnchantmentHelper.getEnchantmentLevel(
                    Enchantment.silkTouch.effectId, stack) > 0;
            if (enabled) {
                removeSilkTouch(stack);
            } else {
                stack.addEnchantment(Enchantment.silkTouch, 1);
            }
            player.addChatMessage(new ChatComponentTranslation(
                    enabled ? "item.hv_vajra.silk.disabled" : "item.hv_vajra.silk.enabled"));
        }
        return stack;
    }

    private void removeSilkTouch(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("ench", 9)) {
            return;
        }

        NBTTagList enchantments = tag.getTagList("ench", 10);
        for (int i = enchantments.tagCount() - 1; i >= 0; i--) {
            if (enchantments.getCompoundTagAt(i).getShort("id")
                    == Enchantment.silkTouch.effectId) {
                enchantments.removeTag(i);
            }
        }
        if (enchantments.tagCount() == 0) {
            tag.removeTag("ench");
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        if (!hasOperationEnergy(stack)) {
            return null;
        }

        ItemStack returned = stack.copy();
        returned.stackSize = 1;
        ElectricItem.manager.discharge(
                returned,
                tier.getOperationCost(),
                tier.getElectricTier(),
                true,
                false,
                false);
        return returned;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getCharge(stack) < tier.getMaxCharge();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - getCharge(stack) / tier.getMaxCharge();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(createStackWithCharge(tier.getMaxCharge()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player,
                               List list, boolean advanced) {
        list.add(EnumChatFormatting.GRAY
                + StatCollector.translateToLocal("item.hv_vajra.tooltip"));
        list.add(EnumChatFormatting.AQUA + ElectricItem.manager.getToolTip(stack));
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        // Registered by ClientEventHandler during TextureStitchEvent.Pre.
    }

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
}
