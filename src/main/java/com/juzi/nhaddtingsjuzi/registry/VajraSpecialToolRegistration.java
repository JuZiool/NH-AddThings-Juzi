package com.juzi.nhaddtingsjuzi.registry;

import java.util.Collection;

final class VajraSpecialToolRegistration {

    private VajraSpecialToolRegistration() {}

    static <T> void register(T tool, Collection<T> allTools,
                             Collection<T> wrenches, Collection<T> wireCutters) {
        if (allTools.contains(tool) || wrenches.contains(tool) || wireCutters.contains(tool)) {
            throw new IllegalStateException("HV Vajra special tool identity is already registered");
        }

        allTools.add(tool);
        wrenches.add(tool);
        wireCutters.add(tool);
    }
}
