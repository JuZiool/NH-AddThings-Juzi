package com.juzi.nhaddtingsjuzi.item;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import appeng.api.implementations.items.IAEWrench;
import org.junit.Test;

public class VajraAeWrenchTest {

    @Test
    public void implementsTheAeWrenchContract() {
        assertTrue(IAEWrench.class.isAssignableFrom(ItemTieredVajra.class));
    }

    @Test
    public void wrenchEnergyGateMatchesOperationCost() {
        assertFalse(VajraLogic.canUseWrench(3332.0D, 3333, false));
        assertTrue(VajraLogic.canUseWrench(3333.0D, 3333, false));
        assertTrue(VajraLogic.canUseWrench(0.0D, 3333, true));
    }

    @Test
    public void alwaysBypassesSneakUseLikeQuartzWrench() {
        assertTrue(VajraLogic.shouldBypassSneakUse(true));
    }
}
