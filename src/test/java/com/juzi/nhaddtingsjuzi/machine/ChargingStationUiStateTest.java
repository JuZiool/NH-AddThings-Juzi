package com.juzi.nhaddtingsjuzi.machine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

public class ChargingStationUiStateTest {

    @Test
    public void keepsRawServerValuesForSynchronization() {
        ChargingStationUiState state = new ChargingStationUiState(
                true, "HV", 8, 4096L, 123456L, 3276800L,
                "Juzi", 2, 7, 64);

        assertTrue(state.isEnabled());
        assertEquals("HV", state.getTierName());
        assertEquals(8, state.getAmperage());
        assertEquals(4096L, state.getOutputEuPerTick());
        assertEquals(123456L, state.getStoredEu());
        assertEquals(3276800L, state.getMaxStoredEu());
        assertEquals("Juzi", state.getOwnerName());
        assertEquals(2, state.getEligibleOnlinePlayers());
        assertEquals(7, state.getCachedTargets());
        assertEquals(64, state.getRadius());
    }

    @Test
    public void rendersWithTranslationKeysAndRawArguments() {
        ChargingStationUiState state = new ChargingStationUiState(
                false, "HV", 8, 4096L, 123456L, 3276800L,
                "Juzi", 2, 7, 64);
        final List<String> keys = new ArrayList<String>();
        ChargingStationUiState.Localizer localizer = new ChargingStationUiState.Localizer() {
            @Override
            public String text(String key) {
                keys.add(key);
                return key;
            }

            @Override
            public String text(String key, Object... arguments) {
                keys.add(key);
                return key + Arrays.toString(arguments);
            }
        };

        String rendered = ChargingStationLogic.compactGuiStatus(
                state.localizedLines(localizer, new Function<Long, String>() {
                    @Override
                    public String apply(Long value) {
                        return String.valueOf(value);
                    }
                }));

        assertTrue(keys.contains("nh_addtings_juzi.charging_station.status"));
        assertTrue(keys.contains("nh_addtings_juzi.charging_station.energy"));
        assertFalse(rendered.contains("Status"));
        assertFalse(rendered.contains("Stored EU"));
        assertTrue(rendered.contains("123456"));
    }
}
