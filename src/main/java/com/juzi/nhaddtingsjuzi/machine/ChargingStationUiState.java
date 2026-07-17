package com.juzi.nhaddtingsjuzi.machine;

import java.util.function.Function;

public final class ChargingStationUiState {

    private final boolean enabled;
    private final String tierName;
    private final int amperage;
    private final long outputEuPerTick;
    private final long storedEu;
    private final long maxStoredEu;
    private final String ownerName;
    private final int eligibleOnlinePlayers;
    private final int cachedTargets;
    private final int radius;

    public ChargingStationUiState(boolean enabled, String tierName, int amperage,
                                  long outputEuPerTick, long storedEu, long maxStoredEu,
                                  String ownerName, int eligibleOnlinePlayers,
                                  int cachedTargets, int radius) {
        this.enabled = enabled;
        this.tierName = tierName;
        this.amperage = amperage;
        this.outputEuPerTick = outputEuPerTick;
        this.storedEu = storedEu;
        this.maxStoredEu = maxStoredEu;
        this.ownerName = ownerName;
        this.eligibleOnlinePlayers = eligibleOnlinePlayers;
        this.cachedTargets = cachedTargets;
        this.radius = radius;
    }

    public static ChargingStationUiState empty() {
        return new ChargingStationUiState(true, "-", 0, 0L, 0L, 0L,
                "-", 0, 0, 0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTierName() {
        return tierName;
    }

    public int getAmperage() {
        return amperage;
    }

    public long getOutputEuPerTick() {
        return outputEuPerTick;
    }

    public long getStoredEu() {
        return storedEu;
    }

    public long getMaxStoredEu() {
        return maxStoredEu;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getEligibleOnlinePlayers() {
        return eligibleOnlinePlayers;
    }

    public int getCachedTargets() {
        return cachedTargets;
    }

    public int getRadius() {
        return radius;
    }

    public ChargingStationUiState withEnabled(boolean value) {
        return new ChargingStationUiState(value, tierName, amperage, outputEuPerTick,
                storedEu, maxStoredEu, ownerName, eligibleOnlinePlayers, cachedTargets, radius);
    }

    public ChargingStationUiState withTierName(String value) {
        return new ChargingStationUiState(enabled, value, amperage, outputEuPerTick,
                storedEu, maxStoredEu, ownerName, eligibleOnlinePlayers, cachedTargets, radius);
    }

    public ChargingStationUiState withAmperage(int value) {
        return new ChargingStationUiState(enabled, tierName, value, outputEuPerTick,
                storedEu, maxStoredEu, ownerName, eligibleOnlinePlayers, cachedTargets, radius);
    }

    public ChargingStationUiState withOutputEuPerTick(long value) {
        return new ChargingStationUiState(enabled, tierName, amperage, value,
                storedEu, maxStoredEu, ownerName, eligibleOnlinePlayers, cachedTargets, radius);
    }

    public ChargingStationUiState withStoredEu(long value) {
        return new ChargingStationUiState(enabled, tierName, amperage, outputEuPerTick,
                value, maxStoredEu, ownerName, eligibleOnlinePlayers, cachedTargets, radius);
    }

    public ChargingStationUiState withMaxStoredEu(long value) {
        return new ChargingStationUiState(enabled, tierName, amperage, outputEuPerTick,
                storedEu, value, ownerName, eligibleOnlinePlayers, cachedTargets, radius);
    }

    public ChargingStationUiState withOwnerName(String value) {
        return new ChargingStationUiState(enabled, tierName, amperage, outputEuPerTick,
                storedEu, maxStoredEu, value, eligibleOnlinePlayers, cachedTargets, radius);
    }

    public ChargingStationUiState withEligibleOnlinePlayers(int value) {
        return new ChargingStationUiState(enabled, tierName, amperage, outputEuPerTick,
                storedEu, maxStoredEu, ownerName, value, cachedTargets, radius);
    }

    public ChargingStationUiState withCachedTargets(int value) {
        return new ChargingStationUiState(enabled, tierName, amperage, outputEuPerTick,
                storedEu, maxStoredEu, ownerName, eligibleOnlinePlayers, value, radius);
    }

    public ChargingStationUiState withRadius(int value) {
        return new ChargingStationUiState(enabled, tierName, amperage, outputEuPerTick,
                storedEu, maxStoredEu, ownerName, eligibleOnlinePlayers, cachedTargets, value);
    }

    public String[] localizedLines(Localizer localizer, Function<Long, String> numberFormatter) {
        return new String[] {
                localizer.text(
                        "nh_addtings_juzi.charging_station.status",
                        localizer.text(enabled
                                ? "nh_addtings_juzi.charging_station.enabled"
                                : "nh_addtings_juzi.charging_station.disabled")),
                localizer.text("nh_addtings_juzi.charging_station.tier", tierName),
                localizer.text("nh_addtings_juzi.charging_station.output",
                        amperage, numberFormatter.apply(outputEuPerTick)),
                localizer.text("nh_addtings_juzi.charging_station.energy",
                        numberFormatter.apply(storedEu), numberFormatter.apply(maxStoredEu)),
                localizer.text("nh_addtings_juzi.charging_station.owner", ownerName),
                localizer.text("nh_addtings_juzi.charging_station.players", eligibleOnlinePlayers),
                localizer.text("nh_addtings_juzi.charging_station.targets", cachedTargets),
                localizer.text("nh_addtings_juzi.charging_station.radius", radius)
        };
    }

    interface Localizer {
        String text(String key);

        String text(String key, Object... arguments);
    }
}
