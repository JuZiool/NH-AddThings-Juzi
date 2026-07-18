package com.juzi.nhaddtingsjuzi.item;

import java.util.List;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.util.GTToolHarvestHelper;
import gregtech.api.util.GTUtility;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemTieredVajra extends Item implements IElectricItem {

    private final VajraTier tier;

    @SideOnly(Side.CLIENT)
    public static IIcon icon;

    private ItemTieredVajra(String unlocalizedName, VajraTier tier) {
        this.tier = tier;
        ((Item) this).setUnlocalizedName(unlocalizedName);
        ((Item) this).setMaxStackSize(1);
        ((Item) this).setNoRepair();
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
        return VajraLogic.isMineableBlock(
                GregTechAPI.isMachineBlock(block, metadata),
                GTToolHarvestHelper.isAppropriateTool(
                        block, metadata, "pickaxe", "shovel", "axe"),
                GTToolHarvestHelper.isAppropriateMaterial(
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
                        Material.gourd),
                GTToolHarvestHelper.isAppropriateMaterial(
                        block,
                        Material.cactus,
                        Material.glass,
                        Material.sponge,
                        Material.cloth,
                        Material.carpet,
                        Material.coral,
                        Material.ice,
                        Material.packedIce,
                        Material.circuits,
                        Material.redstoneLight,
                        Material.tnt,
                        Material.cake,
                        Material.web,
                        Material.piston,
                        Material.dragonEgg));
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
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world,
                                  int x, int y, int z, int side,
                                  float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        Object cable = getGregTechCable(tileEntity);
        if (cable == null) {
            return false;
        }

        if (!VajraLogic.shouldConsumeCableInteraction(true, world.isRemote)) {
            return false;
        }

        ForgeDirection clickedSide = ForgeDirection.getOrientation(side);
        ForgeDirection connectionSide = GTUtility.determineWrenchingSide(
                clickedSide, hitX, hitY, hitZ);
        return useWireCutter(cable, clickedSide, connectionSide,
                player, hitX, hitY, hitZ, stack);
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z,
                                      EntityPlayer player) {
        return VajraLogic.shouldBypassSneakUse(
                isGregTechPipe(world.getTileEntity(x, y, z)));
    }

    private boolean isGregTechPipe(TileEntity tileEntity) {
        if (tileEntity == null) {
            return false;
        }

        try {
            return Class.forName("gregtech.api.metatileentity.BaseMetaPipeEntity")
                    .isInstance(tileEntity);
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private Object getGregTechCable(TileEntity tileEntity) {
        if (tileEntity == null) {
            return null;
        }

        try {
            Class<?> pipeClass = Class.forName(
                    "gregtech.api.metatileentity.BaseMetaPipeEntity");
            if (!pipeClass.isInstance(tileEntity)) {
                return null;
            }
            Object metaTileEntity = pipeClass.getMethod("getMetaTileEntity")
                    .invoke(tileEntity);
            Class<?> cableClass = Class.forName(
                    "gregtech.api.interfaces.metatileentity.IMetaTileEntityCable");
            return cableClass.isInstance(metaTileEntity) ? metaTileEntity : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean useWireCutter(Object cable,
                                  ForgeDirection clickedSide,
                                  ForgeDirection connectionSide,
                                  EntityPlayer player,
                                  float hitX, float hitY, float hitZ,
                                  ItemStack stack) {
        try {
            boolean handled = (Boolean) cable.getClass().getMethod(
                    "onWireCutterRightClick",
                    ForgeDirection.class,
                    ForgeDirection.class,
                    EntityPlayer.class,
                    Float.TYPE,
                    Float.TYPE,
                    Float.TYPE,
                    ItemStack.class).invoke(
                            cable,
                            clickedSide,
                            connectionSide,
                            player,
                            hitX,
                            hitY,
                            hitZ,
                            stack);
            if (handled) {
                cable.getClass().getMethod("func_70296_d").invoke(cable);
            }
            return handled;
        } catch (Exception ignored) {
            return false;
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
