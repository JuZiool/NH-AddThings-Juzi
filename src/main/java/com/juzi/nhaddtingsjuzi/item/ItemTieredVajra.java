package com.juzi.nhaddtingsjuzi.item;

import java.util.List;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.parts.IPartHost;
import appeng.block.AEBaseTileBlock;
import appeng.parts.PartPlacement;
import appeng.parts.PartPlacement.PlaceType;
import appeng.tile.networking.TileCableBus;
import appeng.util.Platform;
import net.minecraft.block.Block;
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
import gregtech.api.util.GTUtility;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ItemTieredVajra extends Item implements IElectricItem, IAEWrench {

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

    private boolean canUseWrench(ItemStack stack, EntityPlayer player) {
        return VajraLogic.canUseWrench(
                getCharge(stack),
                tier.getOperationCost(),
                player != null && player.capabilities.isCreativeMode);
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
        if (!hasOperationEnergy(stack) || !VajraLogic.isMineableBlock()) {
            return 0.0F;
        }
        return tier.getMiningSpeed();
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
        if (ForgeEventFactory.onItemUseStart(player, stack, 1) <= 0) {
            return true;
        }

        Block targetBlock = world.getBlock(x, y, z);
        PlayerInteractEvent.Action action = targetBlock == null || targetBlock.isAir(world, x, y, z)
                ? PlayerInteractEvent.Action.RIGHT_CLICK_AIR
                : PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
        if (ForgeEventFactory.onPlayerInteract(player, action, x, y, z, side, world).isCanceled()) {
            return true;
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        Object cable = getGregTechCable(tileEntity);
        if (cable != null) {
            if (!VajraLogic.shouldConsumeCableInteraction(true, world.isRemote)) {
                return false;
            }

            ForgeDirection clickedSide = ForgeDirection.getOrientation(side);
            ForgeDirection connectionSide = GTUtility.determineWrenchingSide(
                    clickedSide, hitX, hitY, hitZ);
            return useWireCutter(cable, clickedSide, connectionSide,
                    player, hitX, hitY, hitZ, stack);
        }

        if (handleAeWrench(stack, player, world, x, y, z, side, hitX, hitY, hitZ)) {
            return true;
        }

        // Match AE quartz wrench: rotate any non-AE block that supports rotateBlock.
        return handleGenericRotate(stack, player, world, x, y, z, side, targetBlock);
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z,
                                      EntityPlayer player) {
        return VajraLogic.shouldBypassSneakUse(true);
    }

    @Override
    public boolean canWrench(ItemStack stack, EntityPlayer player, int x, int y, int z) {
        return stack != null && stack.getItem() == this && canUseWrench(stack, player);
    }

    private boolean handleAeWrench(ItemStack stack, EntityPlayer player, World world,
                                   int x, int y, int z, int side,
                                   float hitX, float hitY, float hitZ) {
        Block block = world.getBlock(x, y, z);
        if (!(block instanceof AEBaseTileBlock)
                || !canUseWrench(stack, player)
                || !Platform.hasPermissions(
                        new appeng.api.util.DimensionalCoord(world, x, y, z), player)) {
            return false;
        }

        if (!player.isSneaking()) {
            if (world.isRemote) {
                return false;
            }
            if (!block.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side))) {
                return false;
            }
            block.onNeighborBlockChange(world, x, y, z, Platform.AIR_BLOCK);
            player.swingItem();
            consumeWrenchEnergy(stack, player);
            return true;
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof IPartHost) {
            boolean handled = PartPlacement.place(
                    stack,
                    x,
                    y,
                    z,
                    side,
                    player,
                    world,
                    PlaceType.INTERACT_FIRST_PASS,
                    0);
            if (handled && !world.isRemote) {
                consumeWrenchEnergy(stack, player);
            }
            if (handled || tileEntity instanceof TileCableBus) {
                return handled;
            }
        }

        // Let the AE block preserve its own dismantle drops and settings NBT.
        if (world.isRemote) {
            return false;
        }
        Block beforeBlock = world.getBlock(x, y, z);
        TileEntity beforeTile = world.getTileEntity(x, y, z);
        boolean activated = block.onBlockActivated(
                world, x, y, z, player, side, hitX, hitY, hitZ);
        boolean removed = world.getBlock(x, y, z) != beforeBlock
                || world.getTileEntity(x, y, z) != beforeTile;
        if (removed) {
            consumeWrenchEnergy(stack, player);
            return true;
        }
        return activated;
    }

    private boolean handleGenericRotate(ItemStack stack, EntityPlayer player, World world,
                                         int x, int y, int z, int side, Block block) {
        if (player.isSneaking()
                || block == null
                || !canUseWrench(stack, player)
                || !Platform.hasPermissions(
                        new appeng.api.util.DimensionalCoord(world, x, y, z), player)) {
            return false;
        }
        if (world.isRemote) {
            return false;
        }
        if (!block.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side))) {
            return false;
        }
        block.onNeighborBlockChange(world, x, y, z, Platform.AIR_BLOCK);
        player.swingItem();
        consumeWrenchEnergy(stack, player);
        return true;
    }

    private void consumeWrenchEnergy(ItemStack stack, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            ElectricItem.manager.use(stack, tier.getOperationCost(), player);
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
