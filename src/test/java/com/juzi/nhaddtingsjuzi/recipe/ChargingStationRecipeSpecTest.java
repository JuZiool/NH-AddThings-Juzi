package com.juzi.nhaddtingsjuzi.recipe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChargingStationRecipeSpecTest {

    @Test
    public void usesApprovedHvAssemblerRecipe() {
        assertEquals(1, ChargingStationRecipeSpec.EV_HULLS);
        assertEquals(2, ChargingStationRecipeSpec.IV_CIRCUITS);
        assertEquals(16, ChargingStationRecipeSpec.HV_EMITTERS);
        assertEquals(8, ChargingStationRecipeSpec.HV_SENSORS);
        assertEquals(1, ChargingStationRecipeSpec.LV_FIELD_GENERATORS);
        assertEquals(480, ChargingStationRecipeSpec.EUT);
    }
}
