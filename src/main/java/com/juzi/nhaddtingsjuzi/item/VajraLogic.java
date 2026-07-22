package com.juzi.nhaddtingsjuzi.item;

public final class VajraLogic {

    private VajraLogic() {}

    static boolean hasOperationEnergy(double charge, int operationCost) {
        return charge >= operationCost;
    }

    static boolean canUseWrench(double charge, int operationCost, boolean creative) {
        return creative || hasOperationEnergy(charge, operationCost);
    }

    static float miningSpeed(double charge, VajraTier tier) {
        return canHarvest(charge, tier) ? tier.getMiningSpeed() : 0.0F;
    }

    static boolean canHarvest(double charge, VajraTier tier) {
        return hasOperationEnergy(charge, tier.getOperationCost());
    }

    static double transferredCharge(double charge, VajraTier tier) {
        return Math.max(0.0D, Math.min(charge, tier.getMaxCharge()));
    }

    /** HV vajra harvests any block when it has enough charge; material checks are unused. */
    static boolean isMineableBlock() {
        return true;
    }

    static boolean shouldConsumeCableInteraction(boolean cable, boolean clientSide) {
        return cable && !clientSide;
    }

    /** AE quartz wrench always bypasses sneak-use; vajra matches that contract. */
    static boolean shouldBypassSneakUse(boolean always) {
        return always;
    }

    public static boolean shouldSuppressToolMessage(boolean holdingVajra,
                                                    boolean gregTechToolMessage) {
        return holdingVajra && gregTechToolMessage;
    }
}
