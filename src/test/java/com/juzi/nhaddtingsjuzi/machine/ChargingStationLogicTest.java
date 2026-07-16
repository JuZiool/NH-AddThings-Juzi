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
    public void scalesBudgetAndBufferWithCircuitCount() {
        assertEquals(16, ChargingStationLogic.circuitSlotLimit());
        assertEquals(0, ChargingStationLogic.effectiveAmperage(0));
        assertEquals(1, ChargingStationLogic.effectiveAmperage(1));
        assertEquals(8, ChargingStationLogic.effectiveAmperage(8));
        assertEquals(16, ChargingStationLogic.effectiveAmperage(16));
        assertEquals(16, ChargingStationLogic.effectiveAmperage(64));

        assertEquals(0L, ChargingStationLogic.tickBudget(ChargingStationTier.HV, 0));
        assertEquals(512L, ChargingStationLogic.tickBudget(ChargingStationTier.HV, 1));
        assertEquals(4096L, ChargingStationLogic.tickBudget(ChargingStationTier.HV, 8));
        assertEquals(8192L, ChargingStationLogic.tickBudget(ChargingStationTier.HV, 16));
        assertEquals(8192L, ChargingStationLogic.tickBudget(ChargingStationTier.HV, 64));
        assertEquals(204800L, ChargingStationLogic.bufferCapacity(ChargingStationTier.HV, 1));
        assertEquals(3276800L, ChargingStationLogic.bufferCapacity(ChargingStationTier.HV, 16));
        assertEquals(0L, ChargingStationLogic.tickBudget(null, 16));
        assertEquals(0L, ChargingStationLogic.bufferCapacity(null, 16));
    }

    @Test
    public void compactsStatusTierOutputAndRadiusForGui() {
        assertEquals(
                "状态：已启用\n等级：HV  输出：8 A（4,096 EU/t）  半径：64 格\n储存 EU：100 / 200\n所有者：JuZiool\n符合条件的在线玩家：1\n已缓存 GT 机器：2",
                ChargingStationLogic.compactGuiStatus(new String[] {
                        "状态：已启用",
                        "等级：HV",
                        "输出：8 A（4,096 EU/t）",
                        "储存 EU：100 / 200",
                        "所有者：JuZiool",
                        "符合条件的在线玩家：1",
                        "已缓存 GT 机器：2",
                        "半径：64 格"
                }));
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
