package com.juzi.nhaddtingsjuzi.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VajraLogicTest {

    @Test
    public void definesApprovedHvTier() {
        assertEquals(3, VajraTier.HV.getElectricTier());
        assertEquals(10000000, VajraTier.HV.getMaxCharge());
        assertEquals(512.0D, VajraTier.HV.getTransferLimit(), 0.0D);
        assertEquals(3333, VajraTier.HV.getOperationCost());
        assertEquals(30.0F, VajraTier.HV.getMiningSpeed(), 0.0F);
        assertEquals(Integer.MAX_VALUE, VajraTier.HV.getHarvestLevel());
    }

    @Test
    public void requiresOneFullOperationOfCharge() {
        assertFalse(VajraLogic.hasOperationEnergy(3332.0D, 3333));
        assertTrue(VajraLogic.hasOperationEnergy(3333.0D, 3333));
        assertEquals(0.0F, VajraLogic.miningSpeed(3332.0D, VajraTier.HV), 0.0F);
        assertEquals(30.0F, VajraLogic.miningSpeed(3333.0D, VajraTier.HV), 0.0F);
        assertFalse(VajraLogic.canHarvest(3332.0D, VajraTier.HV));
        assertTrue(VajraLogic.canHarvest(3333.0D, VajraTier.HV));
    }

    @Test
    public void allowsAeWrenchUseOnlyWithOneFullOperationOfCharge() {
        assertFalse(VajraLogic.canUseWrench(3332.0D, 3333, false));
        assertTrue(VajraLogic.canUseWrench(3333.0D, 3333, false));
        assertTrue(VajraLogic.canUseWrench(0.0D, 3333, true));
    }

    @Test
    public void capsTransferredBatteryCharge() {
        assertEquals(0.0D, VajraLogic.transferredCharge(-1.0D, VajraTier.HV), 0.0D);
        assertEquals(7500000.0D, VajraLogic.transferredCharge(7500000.0D, VajraTier.HV), 0.0D);
        assertEquals(10000000.0D, VajraLogic.transferredCharge(12000000.0D, VajraTier.HV), 0.0D);
    }

    @Test
    public void minesAnyBlockWhenCharged() {
        assertTrue(VajraLogic.isMineableBlock());
    }

    @Test
    public void onlyConsumesCableInteractionOnTheServer() {
        assertFalse(VajraLogic.shouldConsumeCableInteraction(false, false));
        assertFalse(VajraLogic.shouldConsumeCableInteraction(true, true));
        assertTrue(VajraLogic.shouldConsumeCableInteraction(true, false));
    }

    @Test
    public void bypassesSneakUseLikeAeWrench() {
        assertTrue(VajraLogic.shouldBypassSneakUse(true));
        assertFalse(VajraLogic.shouldBypassSneakUse(false));
    }

    @Test
    public void onlySuppressesGregTechToolMessagesFromTheVajra() {
        assertFalse(VajraLogic.shouldSuppressToolMessage(false, true));
        assertFalse(VajraLogic.shouldSuppressToolMessage(true, false));
        assertTrue(VajraLogic.shouldSuppressToolMessage(true, true));
    }
}
