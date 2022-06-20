package com.TickTracker.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MillisecondDefaultSectionColor {
    YELLOW("Yellow"),
    GREEN("Green");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

}
