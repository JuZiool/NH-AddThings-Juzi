package com.juzi.nhaddtingsjuzi.terminal.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DualTerminalRecipeLimitTest {

    @Test
    public void capsAlternativesPerSlot() {
        assertEquals(16, DualTerminalRecipeHandler.MAX_ALTERNATIVES_PER_SLOT);
    }
}