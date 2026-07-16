package com.juzi.nhaddtingsjuzi.item;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FlightCharmLogicTest {

    @Test
    public void claimsOnlyUnavailableSurvivalFlightWithEnoughFood() {
        assertTrue(FlightCharmLogic.shouldClaimFlight(false, false, false, true));
        assertFalse(FlightCharmLogic.shouldClaimFlight(false, true, false, true));
        assertFalse(FlightCharmLogic.shouldClaimFlight(false, false, true, true));
        assertFalse(FlightCharmLogic.shouldClaimFlight(false, false, false, false));
        assertFalse(FlightCharmLogic.shouldClaimFlight(true, false, false, true));
    }

    @Test
    public void restoresPreviouslyOwnedFlightAfterPlayerLoad() {
        assertTrue(FlightCharmLogic.shouldRestoreOwnedFlight(true, false, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(false, false, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, true, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, false, true, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, false, false, false));
    }

    @Test
    public void releasesOnlyOwnedNonCreativeFlight() {
        assertTrue(FlightCharmLogic.shouldReleaseOwnedFlight(true, false));
        assertFalse(FlightCharmLogic.shouldReleaseOwnedFlight(false, false));
        assertFalse(FlightCharmLogic.shouldReleaseOwnedFlight(true, true));
    }
}
