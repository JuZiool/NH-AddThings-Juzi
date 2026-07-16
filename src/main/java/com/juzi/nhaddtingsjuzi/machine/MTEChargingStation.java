package com.juzi.nhaddtingsjuzi.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.juzi.nhaddtingsjuzi.compat.PlayerElectricInventory;
import com.juzi.nhaddtingsjuzi.compat.ServerUtilitiesTeamResolver;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.metatileentity.implementations.MTEBasicHull;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class MTEChargingStation extends MTEBasicHull implements IAddUIWidgets {

    private static final int CIRCUIT_SLOT = 0;
    private static final Textures.BlockIcons.CustomIcon FRONT_OVERLAY =
            new Textures.BlockIcons.CustomIcon(
                    "nh_addtings_juzi:machine/overlay_charging_station");

    private final ServerUtilitiesTeamResolver teamResolver = new ServerUtilitiesTeamResolver();
    private final List<TargetPosition> targets = new ArrayList<TargetPosition>();
    private final Set<TargetPosition> targetSet = new HashSet<TargetPosition>();
    private List<ItemStack> playerItems = Collections.emptyList();
    private ChargingStationTier activeTier;
    private int discoveryCursor;
    private int serviceCursor;
    private int eligibleOnlinePlayers;
    private boolean enabled = true;

    public MTEChargingStation(int id, String name, String regionalName, int machineTier) {
        super(id, name, regionalName, machineTier,
                "Charges the owner's team and nearby GT machines");
    }

    private MTEChargingStation(String name, int tier, int slots,
                               String[] description, ITexture[][][] textures) {
        super(name, tier, slots, description, textures);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity tileEntity) {
        return new MTEChargingStation(
                mName, mTier, mInventory.length, mDescriptionArray, mTextures);
    }

    @Override
    public void onPostTick(IGregTechTileEntity base, long tick) {
        super.onPostTick(base, tick);
        if (!base.isServerSide()) {
            return;
        }

        ChargingStationTier tier = resolveTier(mInventory[CIRCUIT_SLOT]);
        if (tier != activeTier) {
            activeTier = tier;
            clearTargets();
        }
        if (!enabled || activeTier == null || base.getStoredEU() <= 0L) {
            return;
        }

        if (tick % 20L == 0L) {
            refreshPlayerItems(base);
        }
        discoverTargets(base);

        long budget = Math.min(base.getStoredEU(), ChargingStationLogic.tickBudget(activeTier));
        long playerUsed = chargePlayers(budget);
        long remaining = budget - playerUsed;
        long machineUsed = supplyMachines(base, remaining);
        long used = playerUsed + machineUsed;
        if (used > 0L) {
            base.decreaseStoredEnergyUnits(used, true);
        }
    }

    @Override
    public long maxEUInput() {
        return activeTier == null ? 0L : activeTier.getVoltage();
    }

    @Override
    public long maxAmperesIn() {
        return activeTier == null ? 0L : 16L;
    }

    @Override
    public long maxEUStore() {
        return ChargingStationLogic.bufferCapacity(activeTier);
    }

    @Override
    public boolean isEnetOutput() {
        return false;
    }

    @Override
    public boolean isOutputFacing(ForgeDirection side) {
        return false;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity base,
                                 ForgeDirection side,
                                 ForgeDirection facing,
                                 int colorIndex,
                                 boolean active,
                                 boolean redstone) {
        Textures.BlockIcons casingIcon;
        if (side == ForgeDirection.UP) {
            casingIcon = Textures.BlockIcons.MACHINE_EV_TOP;
        } else if (side == ForgeDirection.DOWN) {
            casingIcon = Textures.BlockIcons.MACHINE_EV_BOTTOM;
        } else {
            casingIcon = Textures.BlockIcons.MACHINE_EV_SIDE;
        }
        ITexture[] casing = new ITexture[] { TextureFactory.of(casingIcon) };
        if (side != facing) {
            return casing;
        }
        ITexture[] front = new ITexture[casing.length + 1];
        System.arraycopy(casing, 0, front, 0, casing.length);
        front[casing.length] = TextureFactory.of(FRONT_OVERLAY);
        return front;
    }

    @Override
    public boolean isValidSlot(int index) {
        return index == CIRCUIT_SLOT;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity base, int index,
                                 ForgeDirection side, ItemStack stack) {
        return index == CIRCUIT_SLOT && resolveTier(stack) != null;
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity base, EntityPlayer player) {
        if (player.isSneaking()) {
            if (base.isServerSide()) {
                enabled = !enabled;
            }
            return true;
        }
        if (base.isServerSide()) {
            openGui(player);
        }
        return true;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext context) {
        builder.bindPlayerInventory(context.getPlayer());
        builder.widget(new SlotWidget(inventoryHandler, CIRCUIT_SLOT)
                .setFilter(new Predicate<ItemStack>() {
                    @Override
                    public boolean test(ItemStack stack) {
                        return resolveTier(stack) != null;
                    }
                })
                .setBackground(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_CIRCUIT)
                .setPos(8, 18));
        builder.widget(TextWidget.dynamicString(new Supplier<String>() {
            @Override
            public String get() {
                return getStatusText();
            }
        }).setSynced(true).setPos(32, 18));
        builder.widget(new CycleButtonWidget()
                .setToggle(new Supplier<Boolean>() {
                    @Override
                    public Boolean get() {
                        return enabled;
                    }
                }, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean value) {
                        enabled = value;
                    }
                })
                .setVariableBackground(
                        GTUITextures.BUTTON_STANDARD,
                        GTUITextures.BUTTON_STANDARD_PRESSED)
                .setTextureGetter(new Function<Integer, com.gtnewhorizons.modularui.api.drawable.IDrawable>() {
                    @Override
                    public com.gtnewhorizons.modularui.api.drawable.IDrawable apply(Integer state) {
                        return state == 0
                                ? GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF
                                : GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_ON;
                    }
                })
                .addTooltip(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer state) {
                        return StatCollector.translateToLocal(
                                state == 0
                                        ? "nh_addtings_juzi.charging_station.disabled"
                                        : "nh_addtings_juzi.charging_station.enabled");
                    }
                })
                .setPos(8, 42)
                .setSize(18, 18));
    }

    private String getStatusText() {
        String[] lines = getInfoData();
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < lines.length; index++) {
            if (index > 0) {
                text.append('\n');
            }
            text.append(lines[index]);
        }
        return text.toString();
    }

    public boolean isGivingInformation() {
        return true;
    }

    public String[] getInfoData() {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        String tierName = activeTier == null ? "-" : activeTier.name();
        int radius = activeTier == null ? 0 : activeTier.getRadius();
        return new String[] {
                StatCollector.translateToLocalFormatted(
                        "nh_addtings_juzi.charging_station.status",
                        enabled
                                ? StatCollector.translateToLocal("nh_addtings_juzi.charging_station.enabled")
                                : StatCollector.translateToLocal("nh_addtings_juzi.charging_station.disabled")),
                StatCollector.translateToLocalFormatted(
                        "nh_addtings_juzi.charging_station.tier", tierName),
                StatCollector.translateToLocalFormatted(
                        "nh_addtings_juzi.charging_station.energy",
                        GTUtility.formatNumbers(base == null ? 0L : base.getStoredEU()),
                        GTUtility.formatNumbers(maxEUStore())),
                StatCollector.translateToLocalFormatted(
                        "nh_addtings_juzi.charging_station.owner",
                        base == null ? "-" : base.getOwnerName()),
                StatCollector.translateToLocalFormatted(
                        "nh_addtings_juzi.charging_station.players", eligibleOnlinePlayers),
                StatCollector.translateToLocalFormatted(
                        "nh_addtings_juzi.charging_station.targets", targets.size()),
                StatCollector.translateToLocalFormatted(
                        "nh_addtings_juzi.charging_station.radius", radius)
        };
    }

    @Override
    public void saveNBTData(NBTTagCompound tag) {
        super.saveNBTData(tag);
        tag.setBoolean("ChargingStationEnabled", enabled);
        tag.setInteger("ChargingStationDiscoveryCursor", discoveryCursor);
        tag.setInteger("ChargingStationServiceCursor", serviceCursor);
    }

    @Override
    public void loadNBTData(NBTTagCompound tag) {
        super.loadNBTData(tag);
        enabled = !tag.hasKey("ChargingStationEnabled")
                || tag.getBoolean("ChargingStationEnabled");
        discoveryCursor = tag.getInteger("ChargingStationDiscoveryCursor");
        serviceCursor = tag.getInteger("ChargingStationServiceCursor");
    }

    private ChargingStationTier resolveTier(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        List<String> oreNames = new ArrayList<String>();
        int[] oreIds = OreDictionary.getOreIDs(stack);
        for (int oreId : oreIds) {
            oreNames.add(OreDictionary.getOreName(oreId));
        }
        return ChargingStationLogic.tierFromOreNames(oreNames);
    }

    private void refreshPlayerItems(IGregTechTileEntity base) {
        UUID owner = base.getOwnerUuid();
        MinecraftServer server = MinecraftServer.getServer();
        List<ItemStack> candidates = new ArrayList<ItemStack>();
        List<EntityPlayerMP> players = teamResolver.resolve(owner, server);
        eligibleOnlinePlayers = players.size();
        for (EntityPlayerMP player : players) {
            candidates.addAll(PlayerElectricInventory.collect(player));
        }
        Collections.sort(candidates, new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack first, ItemStack second) {
                return Double.compare(chargeRatio(first), chargeRatio(second));
            }
        });
        playerItems = candidates;
    }

    private double chargeRatio(ItemStack stack) {
        if (!(stack.getItem() instanceof IElectricItem)) {
            return 1.0D;
        }
        IElectricItem item = (IElectricItem) stack.getItem();
        double maximum = item.getMaxCharge(stack);
        return maximum <= 0.0D ? 1.0D : ElectricItem.manager.getCharge(stack) / maximum;
    }

    private long chargePlayers(long budget) {
        long used = 0L;
        for (ItemStack stack : playerItems) {
            if (used >= budget || !(stack.getItem() instanceof IElectricItem)) {
                continue;
            }
            IElectricItem item = (IElectricItem) stack.getItem();
            if (item.getTier(stack) > activeTier.getElectricTier()) {
                continue;
            }
            double accepted = ElectricItem.manager.charge(
                    stack,
                    budget - used,
                    activeTier.getElectricTier(),
                    false,
                    false);
            used += (long) Math.ceil(accepted);
        }
        return used;
    }

    @SuppressWarnings("unchecked")
    private void discoverTargets(IGregTechTileEntity base) {
        World world = base.getWorld();
        List<TileEntity> loaded = world.loadedTileEntityList;
        if (loaded.isEmpty()) {
            discoveryCursor = 0;
            return;
        }
        int checked = 0;
        while (checked < ChargingStationLogic.discoveryLimit() && checked < loaded.size()) {
            if (discoveryCursor >= loaded.size()) {
                discoveryCursor = 0;
            }
            TileEntity tile = loaded.get(discoveryCursor++);
            checked++;
            if (tile == base || !(tile instanceof IGregTechTileEntity)) {
                continue;
            }
            IGregTechTileEntity target = (IGregTechTileEntity) tile;
            if (!isEligibleMachineTarget(target)) {
                continue;
            }
            if (!ChargingStationLogic.inRange(
                    base.getXCoord(), base.getYCoord(), base.getZCoord(),
                    tile.xCoord, tile.yCoord, tile.zCoord,
                    activeTier.getRadius())) {
                continue;
            }
            TargetPosition position = new TargetPosition(tile.xCoord, tile.yCoord, tile.zCoord);
            if (targetSet.add(position)) {
                targets.add(position);
            }
        }
    }

    private long supplyMachines(IGregTechTileEntity base, long budget) {
        long used = 0L;
        int serviced = 0;
        while (used < budget && serviced < ChargingStationLogic.serviceLimit()
                && !targets.isEmpty()) {
            if (serviceCursor >= targets.size()) {
                serviceCursor = 0;
            }
            TargetPosition position = targets.get(serviceCursor);
            if (!base.getWorld().blockExists(position.x, position.y, position.z)) {
                removeTarget(serviceCursor);
                continue;
            }
            TileEntity tile = base.getWorld().getTileEntity(position.x, position.y, position.z);
            if (!(tile instanceof IGregTechTileEntity)
                    || !ChargingStationLogic.inRange(
                            base.getXCoord(), base.getYCoord(), base.getZCoord(),
                            position.x, position.y, position.z,
                            activeTier.getRadius())) {
                removeTarget(serviceCursor);
                continue;
            }

            IGregTechTileEntity target = (IGregTechTileEntity) tile;
            if (!isEligibleMachineTarget(target)) {
                removeTarget(serviceCursor);
                continue;
            }
            long targetVoltage = target.getInputVoltage();
            long voltage = ChargingStationLogic.transferVoltage(
                    activeTier.getVoltage(), targetVoltage);
            long availableAmperes = voltage <= 0L ? 0L : (budget - used) / voltage;
            long amperes = Math.min(16L, availableAmperes);
            if (amperes > 0L) {
                long accepted = injectFromAnySide(target, voltage, amperes);
                used += accepted * voltage;
            }
            serviceCursor = ChargingStationLogic.nextCursor(serviceCursor, targets.size());
            serviced++;
        }
        return used;
    }

    private long injectFromAnySide(IGregTechTileEntity target, long voltage, long amperes) {
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if (target.inputEnergyFrom(side)) {
                return target.injectEnergyUnits(side, voltage, amperes);
            }
        }
        return 0L;
    }

    private boolean isEligibleMachineTarget(IGregTechTileEntity target) {
        boolean chargingStation = target.getMetaTileEntity() instanceof MTEChargingStation;
        boolean hasEnergyInput = false;
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if (target.inputEnergyFrom(side)) {
                hasEnergyInput = true;
                break;
            }
        }
        return ChargingStationLogic.isEligibleMachineTarget(
                chargingStation, target.getInputVoltage(), hasEnergyInput);
    }

    private void removeTarget(int index) {
        TargetPosition removed = targets.remove(index);
        targetSet.remove(removed);
        if (serviceCursor >= targets.size()) {
            serviceCursor = 0;
        }
    }

    private void clearTargets() {
        targets.clear();
        targetSet.clear();
        discoveryCursor = 0;
        serviceCursor = 0;
    }

    private static final class TargetPosition {
        private final int x;
        private final int y;
        private final int z;

        private TargetPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof TargetPosition)) return false;
            TargetPosition other = (TargetPosition) object;
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }
}
