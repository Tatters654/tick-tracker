package com.TickTracker.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SmallOverlayStyle {
    NONE("Off"),
    PERCENTAGE("Percentage Good"),
    LAST_DIFF("Last Tick ms"),
    BOTH("Both");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}
