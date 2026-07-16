package com.juzi.nhaddtingsjuzi.item;

final class VajraTier {

    static final VajraTier HV = new VajraTier(
            3, 10000000, 512.0D, 3333, 30.0F, Integer.MAX_VALUE);

    private final int electricTier;
    private final int maxCharge;
    private final double transferLimit;
    private final int operationCost;
    private final float miningSpeed;
    private final int harvestLevel;

    private VajraTier(int electricTier, int maxCharge, double transferLimit,
                      int operationCost, float miningSpeed, int harvestLevel) {
        this.electricTier = electricTier;
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.operationCost = operationCost;
        this.miningSpeed = miningSpeed;
        this.harvestLevel = harvestLevel;
    }

    int getElectricTier() {
        return electricTier;
    }

    int getMaxCharge() {
        return maxCharge;
    }

    double getTransferLimit() {
        return transferLimit;
    }

    int getOperationCost() {
        return operationCost;
    }

    float getMiningSpeed() {
        return miningSpeed;
    }

    int getHarvestLevel() {
        return harvestLevel;
    }
}
