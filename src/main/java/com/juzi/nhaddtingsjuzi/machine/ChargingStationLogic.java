package com.juzi.nhaddtingsjuzi.machine;

import java.util.List;

public final class ChargingStationLogic {

    private static final int AMPERAGE = 16;
    private static final int BUFFER_TICKS = 400;
    private static final int DISCOVERY_LIMIT = 128;
    private static final int SERVICE_LIMIT = 16;

    private ChargingStationLogic() {}

    public static long tickBudget(ChargingStationTier tier) {
        return tier == null ? 0L : tier.getVoltage() * AMPERAGE;
    }

    public static ChargingStationTier tierFromOreNames(List<String> oreNames) {
        for (String oreName : oreNames) {
            ChargingStationTier tier = ChargingStationTier.fromCircuit(oreName);
            if (tier != null) {
                return tier;
            }
        }
        return null;
    }

    public static long bufferCapacity(ChargingStationTier tier) {
        return tickBudget(tier) * BUFFER_TICKS;
    }

    public static long transferVoltage(long stationVoltage, long targetVoltage) {
        if (stationVoltage <= 0L || targetVoltage <= 0L) {
            return 0L;
        }
        return Math.min(stationVoltage, targetVoltage);
    }

    public static int discoveryLimit() {
        return DISCOVERY_LIMIT;
    }

    public static int serviceLimit() {
        return SERVICE_LIMIT;
    }

    public static int nextCursor(int cursor, int size) {
        return size <= 0 ? 0 : (cursor + 1) % size;
    }

    public static boolean inRange(int sourceX, int sourceY, int sourceZ,
                                  int targetX, int targetY, int targetZ,
                                  int radius) {
        long deltaX = targetX - sourceX;
        long deltaY = targetY - sourceY;
        long deltaZ = targetZ - sourceZ;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
                <= (long) radius * radius;
    }

    public static boolean isEligibleMachineTarget(boolean chargingStation,
                                                   long inputVoltage,
                                                   boolean hasEnergyInput) {
        return !chargingStation && inputVoltage > 0L && hasEnergyInput;
    }
}
