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
        assertTrue(FlightCharmLogic.shouldRestoreOwnedFlight(true, true, false, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(false, true, false, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, true, true, false, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, true, false, true, true));
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, true, false, false, false));
    }

    @Test
    public void releasesOnlyOwnedNonCreativeFlight() {
        assertTrue(FlightCharmLogic.shouldReleaseOwnedFlight(true, true, false));
        assertFalse(FlightCharmLogic.shouldReleaseOwnedFlight(false, true, false));
        assertFalse(FlightCharmLogic.shouldReleaseOwnedFlight(true, true, true));
    }

    @Test
    public void countsOnlyActualOwnedSurvivalFlightWithEnoughFood() {
        assertTrue(FlightCharmLogic.shouldCountFlight(true, true, true, false, true));
        assertFalse(FlightCharmLogic.shouldCountFlight(false, true, true, false, true));
        assertFalse(FlightCharmLogic.shouldCountFlight(true, true, false, false, true));
        assertFalse(FlightCharmLogic.shouldCountFlight(true, true, true, true, true));
        assertFalse(FlightCharmLogic.shouldCountFlight(true, true, true, false, false));
    }

    @Test
    public void resetsOwnershipWhenStoredOwnerDiffersFromCurrentPlayer() {
        assertTrue(FlightCharmLogic.shouldResetFlightOwner("owner-a", "owner-b"));
        assertTrue(FlightCharmLogic.shouldResetFlightOwner("", "owner-b"));
        assertFalse(FlightCharmLogic.shouldResetFlightOwner("owner-a", "owner-a"));
    }

    @Test
    public void staleOwnershipCannotRestoreChargeOrReleaseFlight() {
        assertFalse(FlightCharmLogic.shouldRestoreOwnedFlight(true, false, false, false, true));
        assertFalse(FlightCharmLogic.shouldCountFlight(true, false, true, false, true));
        assertFalse(FlightCharmLogic.shouldReleaseOwnedFlight(true, false, false));
    }

    @Test
    public void transferredCharmCanFreshlyClaimFlightAfterOwnershipReset() {
        assertTrue(FlightCharmLogic.shouldResetFlightOwner("owner-a", "owner-b"));
        assertTrue(FlightCharmLogic.shouldClaimFlight(false, false, false, true));
    }

    @Test
    public void matchingOwnerCanReleaseOwnedFlightOnUnequip() {
        assertFalse(FlightCharmLogic.shouldResetFlightOwner("owner-a", "owner-a"));
        assertTrue(FlightCharmLogic.shouldReleaseOwnedFlight(true, true, false));
    }

    @Test
    public void chargesAndResetsOnTheSixHundredthEligibleTick() {
        assertFalse(FlightCharmLogic.shouldChargeOnNextTick(598, 600));
        assertTrue(FlightCharmLogic.shouldChargeOnNextTick(599, 600));
        org.junit.Assert.assertEquals(599, FlightCharmLogic.nextTimer(598, 600));
        org.junit.Assert.assertEquals(0, FlightCharmLogic.nextTimer(599, 600));
    }
}
