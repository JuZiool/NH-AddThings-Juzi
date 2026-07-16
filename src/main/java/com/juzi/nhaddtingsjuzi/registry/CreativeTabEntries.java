package com.juzi.nhaddtingsjuzi.registry;

import java.util.List;

public final class CreativeTabEntries {

    private CreativeTabEntries() {}

    public static <T> void append(List<T> entries, T entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }
}
