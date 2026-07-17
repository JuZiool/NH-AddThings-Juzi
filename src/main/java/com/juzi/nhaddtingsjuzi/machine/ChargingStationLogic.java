package com.juzi.nhaddtingsjuzi.machine;

import java.util.List;

public final class ChargingStationLogic {

    private static final int MAX_AMPERAGE = 16;
    private static final int CIRCUIT_SLOT_LIMIT = 16;
    private static final int BUFFER_TICKS = 400;
    private static final int DISCOVERY_LIMIT = 128;
    private static final int SERVICE_LIMIT = 16;

    private ChargingStationLogic() {}

    public static int circuitSlotLimit() {
        return CIRCUIT_SLOT_LIMIT;
    }

    public static int effectiveAmperage(int circuitCount) {
        return Math.max(0, Math.min(circuitCount, MAX_AMPERAGE));
    }

    public static long tickBudget(ChargingStationTier tier, int circuitCount) {
        return tier == null ? 0L : tier.getVoltage() * effectiveAmperage(circuitCount);
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

    public static long bufferCapacity(ChargingStationTier tier, int circuitCount) {
        return tickBudget(tier, circuitCount) * BUFFER_TICKS;
    }

    public static String compactGuiStatus(String[] lines) {
        if (lines == null || lines.length < 8) {
            return "";
        }
        StringBuilder text = new StringBuilder(lines[0]);
        text.append("  ").append(lines[7])
                .append('\n')
                .append(lines[1]).append("  ")
                .append(lines[2]);
        for (int index = 3; index < 7; index++) {
            text.append('\n').append(lines[index]);
        }
        return text.toString();
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
