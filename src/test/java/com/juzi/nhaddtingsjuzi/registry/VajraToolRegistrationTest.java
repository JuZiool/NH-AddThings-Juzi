package com.juzi.nhaddtingsjuzi.registry;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class VajraToolRegistrationTest {

    @Test
    public void registersInGlobalWrenchAndWireCutterCollections() {
        Set<String> allTools = new HashSet<String>();
        Set<String> wrenches = new HashSet<String>();
        Set<String> wireCutters = new HashSet<String>();

        VajraSpecialToolRegistration.register(
                "vajra", allTools, wrenches, wireCutters);

        assertTrue(allTools.contains("vajra"));
        assertTrue(wrenches.contains("vajra"));
        assertTrue(wireCutters.contains("vajra"));
    }
}
