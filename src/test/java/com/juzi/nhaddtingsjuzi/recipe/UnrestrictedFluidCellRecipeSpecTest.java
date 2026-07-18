package com.juzi.nhaddtingsjuzi.recipe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.juzi.nhaddtingsjuzi.storage.UnrestrictedFluidCellComponents;

public class UnrestrictedFluidCellRecipeSpecTest {

    @Test
    public void mapsTheFourFluidCellTiersToAe2FluidParts() {
        assertEquals(0, UnrestrictedFluidCellComponents.damageForCapacity(1024));
        assertEquals(1, UnrestrictedFluidCellComponents.damageForCapacity(4096));
        assertEquals(2, UnrestrictedFluidCellComponents.damageForCapacity(16384));
        assertEquals(3, UnrestrictedFluidCellComponents.damageForCapacity(65536));
    }
}
