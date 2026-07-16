package com.juzi.nhaddtingsjuzi.item;

final class FlightCharmLogic {

    private FlightCharmLogic() {}

    static boolean shouldClaimFlight(boolean ownsFlight, boolean allowFlying,
                                     boolean creative, boolean hasEnoughFood) {
        return !ownsFlight && !allowFlying && !creative && hasEnoughFood;
    }

    static boolean shouldRestoreOwnedFlight(boolean ownsFlight, boolean allowFlying,
                                            boolean creative, boolean hasEnoughFood) {
        return ownsFlight && !allowFlying && !creative && hasEnoughFood;
    }

    static boolean shouldReleaseOwnedFlight(boolean ownsFlight, boolean creative) {
        return ownsFlight && !creative;
    }
}
