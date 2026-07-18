package com.juzi.nhaddtingsjuzi.storage;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnrestrictedFluidCellStorageLogicTest {

    @Test
    public void convertsCapacityBytesToFluidAmount() {
        assertEquals(8192000L, UnrestrictedFluidCellStorageLogic.capacityForBytes(1024));
        assertEquals(524288000L, UnrestrictedFluidCellStorageLogic.capacityForBytes(65536));
    }

    @Test
    public void roundsUsedBytesUpLikeTheItemCell() {
        assertEquals(0L, UnrestrictedFluidCellStorageLogic.usedBytes(0));
        assertEquals(1L, UnrestrictedFluidCellStorageLogic.usedBytes(1));
        assertEquals(1L, UnrestrictedFluidCellStorageLogic.usedBytes(8000));
        assertEquals(2L, UnrestrictedFluidCellStorageLogic.usedBytes(8001));
    }

    @Test
    public void computesRemainingFluidFromTheExactAmountLedger() {
        assertEquals(8192000L, UnrestrictedFluidCellStorageLogic.remainingAmount(1024, 0));
        assertEquals(8191999L, UnrestrictedFluidCellStorageLogic.remainingAmount(1024, 1));
        assertEquals(0L, UnrestrictedFluidCellStorageLogic.remainingAmount(1024, 8192000L));
        assertEquals(0L, UnrestrictedFluidCellStorageLogic.remainingAmount(1024, 8192001L));
    }
}
