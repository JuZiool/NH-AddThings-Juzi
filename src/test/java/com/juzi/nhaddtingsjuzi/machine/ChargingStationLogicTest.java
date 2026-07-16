package com.juzi.nhaddtingsjuzi.machine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.Arrays;

import org.junit.Test;

public class ChargingStationLogicTest {

    @Test
    public void mapsRealCircuitsFromLvThroughIv() {
        assertEquals(ChargingStationTier.LV, ChargingStationTier.fromCircuit("circuitBasic"));
        assertEquals(ChargingStationTier.MV, ChargingStationTier.fromCircuit("circuitGood"));
        assertEquals(ChargingStationTier.HV, ChargingStationTier.fromCircuit("circuitAdvanced"));
        assertEquals(ChargingStationTier.EV, ChargingStationTier.fromCircuit("circuitData"));
        assertEquals(ChargingStationTier.IV, ChargingStationTier.fromCircuit("circuitElite"));
        assertNull(ChargingStationTier.fromCircuit("circuitMaster"));
        assertNull(ChargingStationTier.fromCircuit(null));
    }

    @Test
    public void mapsCircuitFromAnyForgeOreDictionaryName() {
        assertEquals(ChargingStationTier.LV, ChargingStationLogic.tierFromOreNames(
                Arrays.asList("craftingCircuitTier01", "circuitBasic")));
        assertEquals(ChargingStationTier.IV, ChargingStationLogic.tierFromOreNames(
                Arrays.asList("circuitElite")));
        assertNull(ChargingStationLogic.tierFromOreNames(
                Arrays.asList("circuitMaster", "plateSteel")));
    }

    @Test
    public void definesTierVoltageRangeAndElectricTier() {
        assertEquals(1, ChargingStationTier.LV.getElectricTier());
        assertEquals(32L, ChargingStationTier.LV.getVoltage());
        assertEquals(16, ChargingStationTier.LV.getRadius());
        assertEquals(2, ChargingStationTier.MV.getElectricTier());
        assertEquals(128L, ChargingStationTier.MV.getVoltage());
        assertEquals(32, ChargingStationTier.MV.getRadius());
        assertEquals(3, ChargingStationTier.HV.getElectricTier());
        assertEquals(512L, ChargingStationTier.HV.getVoltage());
        assertEquals(64, ChargingStationTier.HV.getRadius());
        assertEquals(4, ChargingStationTier.EV.getElectricTier());
        assertEquals(2048L, ChargingStationTier.EV.getVoltage());
        assertEquals(128, ChargingStationTier.EV.getRadius());
        assertEquals(5, ChargingStationTier.IV.getElectricTier());
        assertEquals(8192L, ChargingStationTier.IV.getVoltage());
        assertEquals(256, ChargingStationTier.IV.getRadius());
    }

    @Test
    public void calculatesSixteenAmpBudgetAndTwentySecondBuffer() {
        assertEquals(8192L, ChargingStationLogic.tickBudget(ChargingStationTier.HV));
        assertEquals(3276800L, ChargingStationLogic.bufferCapacity(ChargingStationTier.HV));
        assertEquals(0L, ChargingStationLogic.tickBudget(null));
        assertEquals(0L, ChargingStationLogic.bufferCapacity(null));
    }

    @Test
    public void safelyCapsVoltageForLowerTierTargets() {
        assertEquals(32L, ChargingStationLogic.transferVoltage(512L, 32L));
        assertEquals(128L, ChargingStationLogic.transferVoltage(512L, 128L));
        assertEquals(512L, ChargingStationLogic.transferVoltage(512L, 2048L));
        assertEquals(0L, ChargingStationLogic.transferVoltage(512L, 0L));
    }

    @Test
    public void enforcesIncrementalLimitsAndRoundRobin() {
        assertEquals(128, ChargingStationLogic.discoveryLimit());
        assertEquals(16, ChargingStationLogic.serviceLimit());
        assertEquals(0, ChargingStationLogic.nextCursor(0, 0));
        assertEquals(0, ChargingStationLogic.nextCursor(3, 4));
        assertEquals(3, ChargingStationLogic.nextCursor(2, 5));
    }

    @Test
    public void checksRangeWithoutSquareRoots() {
        assertEquals(true, ChargingStationLogic.inRange(0, 0, 0, 16, 0, 0, 16));
        assertEquals(false, ChargingStationLogic.inRange(0, 0, 0, 17, 0, 0, 16));
        assertEquals(false, ChargingStationLogic.inRange(0, 0, 0, 16, 16, 16, 16));
    }

    @Test
    public void onlyCachesExternalGtEnergyInputs() {
        assertEquals(false, ChargingStationLogic.isEligibleMachineTarget(true, 512L, true));
        assertEquals(false, ChargingStationLogic.isEligibleMachineTarget(false, 0L, false));
        assertEquals(false, ChargingStationLogic.isEligibleMachineTarget(false, 512L, false));
        assertEquals(true, ChargingStationLogic.isEligibleMachineTarget(false, 512L, true));
    }
}
