package com.juzi.nhaddtingsjuzi.machine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

import net.minecraft.item.ItemStack;

public class ChargingStationUiSpecTest {

    @Test
    public void doesNotAddLegacyDescription() {
        assertEquals("", ChargingStationUiSpec.description());
    }
    @Test
    public void exposesGtItemTooltipHookAndLocalizedText() throws Exception {
        Method tooltipHook = MTEChargingStation.class.getDeclaredMethod(
                "addAdditionalTooltipInformation", ItemStack.class, List.class);
        assertNotNull(tooltipHook);

        assertTrue(languageResourceContains("zh_CN", "nh_addtings_juzi.charging_station.tooltip"));
        assertTrue(languageResourceContains("en_US", "nh_addtings_juzi.charging_station.tooltip"));
    }

    private boolean languageResourceContains(String language, String key) throws IOException {
        String resource = "/assets/nh_addtings_juzi/lang/" + language + ".lang";
        InputStream stream = getClass().getResourceAsStream(resource);
        try {
            assertNotNull(stream);
            byte[] bytes = new byte[8192];
            int length = stream.read(bytes);
            String contents = new String(bytes, 0, length, StandardCharsets.UTF_8);
            return contents.contains(key + "=");
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
