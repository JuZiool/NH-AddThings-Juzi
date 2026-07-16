package com.juzi.nhaddtingsjuzi.item;

final class FlightCharmLogic {

    private FlightCharmLogic() {}

    static boolean shouldClaimFlight(boolean ownsFlight, boolean allowFlying,
                                     boolean creative, boolean hasEnoughFood) {
        return !ownsFlight && !allowFlying && !creative && hasEnoughFood;
    }

    static boolean shouldResetFlightOwner(String storedOwnerUuid, String currentPlayerUuid) {
        return storedOwnerUuid == null || currentPlayerUuid == null
                || !storedOwnerUuid.equals(currentPlayerUuid);
    }

    static boolean shouldRestoreOwnedFlight(boolean ownsFlight, boolean ownerMatches,
                                            boolean allowFlying, boolean creative,
                                            boolean hasEnoughFood) {
        return ownsFlight && ownerMatches && !allowFlying && !creative && hasEnoughFood;
    }

    static boolean shouldReleaseOwnedFlight(boolean ownsFlight, boolean ownerMatches,
                                            boolean creative) {
        return ownsFlight && ownerMatches && !creative;
    }

    static boolean shouldCountFlight(boolean ownsFlight, boolean ownerMatches,
                                     boolean isFlying, boolean creative,
                                     boolean hasEnoughFood) {
        return ownsFlight && ownerMatches && isFlying && !creative && hasEnoughFood;
    }

    static boolean shouldChargeOnNextTick(int timer, int interval) {
        return timer + 1 >= interval;
    }

    static int nextTimer(int timer, int interval) {
        return shouldChargeOnNextTick(timer, interval) ? 0 : timer + 1;
    }
}
