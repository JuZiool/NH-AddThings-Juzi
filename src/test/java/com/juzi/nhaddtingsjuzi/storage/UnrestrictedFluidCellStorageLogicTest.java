package com.juzi.nhaddtingsjuzi.storage;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnrestrictedFluidCellStorageLogicTest {

    @Test
    public void convertsCapacityBytesToFluidAmount() {
        assertEquals(2097152L, UnrestrictedFluidCellStorageLogic.capacityForBytes(1024));
        assertEquals(8388608L, UnrestrictedFluidCellStorageLogic.capacityForBytes(4096));
        assertEquals(33554432L, UnrestrictedFluidCellStorageLogic.capacityForBytes(16384));
        assertEquals(134217728L, UnrestrictedFluidCellStorageLogic.capacityForBytes(65536));
    }

    @Test
    public void roundsUsedBytesUpLikeTheItemCell() {
        assertEquals(0L, UnrestrictedFluidCellStorageLogic.usedBytes(0));
        assertEquals(1L, UnrestrictedFluidCellStorageLogic.usedBytes(1));
        assertEquals(1L, UnrestrictedFluidCellStorageLogic.usedBytes(2048));
        assertEquals(2L, UnrestrictedFluidCellStorageLogic.usedBytes(2049));
    }

    @Test
    public void computesRemainingFluidFromTheExactAmountLedger() {
        assertEquals(2097152L, UnrestrictedFluidCellStorageLogic.remainingAmount(1024, 0));
        assertEquals(2097151L, UnrestrictedFluidCellStorageLogic.remainingAmount(1024, 1));
        assertEquals(0L, UnrestrictedFluidCellStorageLogic.remainingAmount(1024, 2097152L));
        assertEquals(0L, UnrestrictedFluidCellStorageLogic.remainingAmount(1024, 2097153L));
    }
}
