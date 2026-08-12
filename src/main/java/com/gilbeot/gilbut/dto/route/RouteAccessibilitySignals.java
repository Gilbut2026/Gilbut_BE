package com.gilbeot.gilbut.dto.route;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteAccessibilitySignals {

    private Signal stair;
    private Signal overpass;
    private Signal underpass;

    public static RouteAccessibilitySignals known(
            int stairCount,
            int overpassCount,
            int underpassCount
    ) {
        return RouteAccessibilitySignals.builder()
                .stair(knownSignal(stairCount))
                .overpass(knownSignal(overpassCount))
                .underpass(knownSignal(underpassCount))
                .build();
    }

    public static RouteAccessibilitySignals unknown() {
        return RouteAccessibilitySignals.builder()
                .stair(unknownSignal())
                .overpass(unknownSignal())
                .underpass(unknownSignal())
                .build();
    }

    private static Signal knownSignal(
            int count
    ) {
        int normalizedCount = Math.max(count, 0);

        return Signal.builder()
                .state(
                        normalizedCount > 0
                                ? State.PRESENT
                                : State.ABSENT
                )
                .count(normalizedCount)
                .build();
    }

    private static Signal unknownSignal() {
        return Signal.builder()
                .state(State.UNKNOWN)
                .count(null)
                .build();
    }

    @Getter
    @Builder
    public static class Signal {

        private State state;
        private Integer count;
    }

    public enum State {
        PRESENT,
        ABSENT,
        UNKNOWN
    }
}