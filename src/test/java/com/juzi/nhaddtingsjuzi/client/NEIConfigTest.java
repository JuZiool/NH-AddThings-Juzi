package com.juzi.nhaddtingsjuzi.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import codechicken.nei.api.IConfigureNEI;

public class NEIConfigTest {

    @Test
    public void exposesChargingStationThroughNeiConfiguration() {
        NEIConfig config = new NEIConfig();

        assertTrue(config instanceof IConfigureNEI);
        assertEquals("NH-AddTings-Juzi", config.getName());
        assertEquals("0.1.5b", config.getVersion());
    }
}
