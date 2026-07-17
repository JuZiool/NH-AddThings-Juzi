package com.juzi.nhaddtingsjuzi.machine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChargingStationUiSpecTest {

    @Test
    public void describesChargingStationBehavior() {
        assertEquals(
                "无线为队伍成员和附近 GregTech 机器充电；电路等级决定电压、安培数和服务半径",
                ChargingStationUiSpec.description());
    }
}
