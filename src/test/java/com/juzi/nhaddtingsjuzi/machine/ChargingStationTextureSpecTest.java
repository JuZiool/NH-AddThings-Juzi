package com.juzi.nhaddtingsjuzi.machine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChargingStationTextureSpecTest {

    @Test
    public void usesEvMachineHullAsTheBaseTexture() {
        assertEquals(4, ChargingStationTextureSpec.MACHINE_TIER);
    }
}
