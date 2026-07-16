package com.juzi.nhaddtingsjuzi.machine;

public enum ChargingStationTier {
    LV("circuitBasic", 1, 32L, 16),
    MV("circuitGood", 2, 128L, 32),
    HV("circuitAdvanced", 3, 512L, 64),
    EV("circuitData", 4, 2048L, 128),
    IV("circuitElite", 5, 8192L, 256);

    private final String circuitName;
    private final int electricTier;
    private final long voltage;
    private final int radius;

    ChargingStationTier(String circuitName, int electricTier, long voltage, int radius) {
        this.circuitName = circuitName;
        this.electricTier = electricTier;
        this.voltage = voltage;
        this.radius = radius;
    }

    public int getElectricTier() {
        return electricTier;
    }

    public long getVoltage() {
        return voltage;
    }

    public int getRadius() {
        return radius;
    }

    public static ChargingStationTier fromCircuit(String circuitName) {
        if (circuitName == null) {
            return null;
        }
        for (ChargingStationTier tier : values()) {
            if (tier.circuitName.equals(circuitName)) {
                return tier;
            }
        }
        return null;
    }
}
