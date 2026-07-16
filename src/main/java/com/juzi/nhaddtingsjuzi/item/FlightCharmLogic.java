package com.juzi.nhaddtingsjuzi.item;

final class FlightCharmLogic {

    private FlightCharmLogic() {}

    static boolean shouldEnableFlight(boolean allowFlying, boolean creative,
                                      boolean hasEnoughFood) {
        return !allowFlying && !creative && hasEnoughFood;
    }

    static boolean shouldDisableFlight(boolean allowFlying, boolean isFlying,
                                       boolean creative) {
        return (allowFlying || isFlying) && !creative;
    }

    static boolean shouldCountFlight(boolean isFlying, boolean creative,
                                     boolean hasEnoughFood) {
        return isFlying && !creative && hasEnoughFood;
    }

    static boolean shouldChargeOnNextTick(int timer, int interval) {
        return timer + 1 >= interval;
    }

    static int nextTimer(int timer, int interval) {
        return shouldChargeOnNextTick(timer, interval) ? 0 : timer + 1;
    }
}
