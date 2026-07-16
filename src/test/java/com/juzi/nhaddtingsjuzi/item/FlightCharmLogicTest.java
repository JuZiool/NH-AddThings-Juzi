package com.juzi.nhaddtingsjuzi.item;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FlightCharmLogicTest {

    @Test
    public void enablesFlightOnlyWhenSurvivalPlayerNeedsItAndHasEnoughFood() {
        assertTrue(FlightCharmLogic.shouldEnableFlight(false, false, true));
        assertFalse(FlightCharmLogic.shouldEnableFlight(true, false, true));
        assertFalse(FlightCharmLogic.shouldEnableFlight(false, true, true));
        assertFalse(FlightCharmLogic.shouldEnableFlight(false, false, false));
    }

    @Test
    public void disablesFlightOnUnequipForEveryNonCreativePlayer() {
        assertTrue(FlightCharmLogic.shouldDisableFlight(true, false, false));
        assertTrue(FlightCharmLogic.shouldDisableFlight(false, true, false));
        assertFalse(FlightCharmLogic.shouldDisableFlight(false, false, false));
        assertFalse(FlightCharmLogic.shouldDisableFlight(true, true, true));
    }

    @Test
    public void countsOnlyActualSurvivalFlightWithEnoughFood() {
        assertTrue(FlightCharmLogic.shouldCountFlight(true, false, true));
        assertFalse(FlightCharmLogic.shouldCountFlight(false, false, true));
        assertFalse(FlightCharmLogic.shouldCountFlight(true, true, true));
        assertFalse(FlightCharmLogic.shouldCountFlight(true, false, false));
    }

    @Test
    public void chargesAndResetsOnTheSixHundredthEligibleTick() {
        assertFalse(FlightCharmLogic.shouldChargeOnNextTick(598, 600));
        assertTrue(FlightCharmLogic.shouldChargeOnNextTick(599, 600));
        org.junit.Assert.assertEquals(599, FlightCharmLogic.nextTimer(598, 600));
        org.junit.Assert.assertEquals(0, FlightCharmLogic.nextTimer(599, 600));
    }
}
