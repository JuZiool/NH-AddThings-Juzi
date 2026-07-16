package com.juzi.nhaddtingsjuzi.item;

public final class VajraLogic {

    private VajraLogic() {}

    static boolean hasOperationEnergy(double charge, int operationCost) {
        return charge >= operationCost;
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

    static boolean isMineableBlock(boolean gregTechMachine,
                                   boolean appropriateTool,
                                   boolean appropriateMaterial) {
        return gregTechMachine || appropriateTool || appropriateMaterial;
    }

    static boolean shouldConsumeCableInteraction(boolean cable, boolean clientSide) {
        return cable && !clientSide;
    }

    static boolean shouldBypassSneakUse(boolean gregTechPipe) {
        return gregTechPipe;
    }

    public static boolean shouldSuppressToolMessage(boolean holdingVajra,
                                                    boolean gregTechToolMessage) {
        return holdingVajra && gregTechToolMessage;
    }
}
