package com.juzi.nhaddtingsjuzi.registry;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CreativeTabEntriesTest {

    @Test
    public void appendsRegisteredMachineEntry() {
        List<String> entries = new ArrayList<String>();

        CreativeTabEntries.append(entries, "charging_station");

        assertEquals(1, entries.size());
        assertEquals("charging_station", entries.get(0));
    }

    @Test
    public void ignoresUnavailableMachineEntry() {
        List<String> entries = new ArrayList<String>();

        CreativeTabEntries.append(entries, null);

        assertEquals(0, entries.size());
    }
}
