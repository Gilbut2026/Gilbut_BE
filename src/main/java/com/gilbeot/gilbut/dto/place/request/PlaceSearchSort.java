package com.gilbeot.gilbut.dto.place.request;

import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@RequiredArgsConstructor
public enum PlaceSearchSort {

    ACCURACY(
            "accuracy",
            "A"
    ),

    DISTANCE(
            "distance",
            "R"
    );

    private final String requestValue;
    private final String tmapCode;

    public static PlaceSearchSort from(
            String value,
            boolean hasCoordinates
    ) {
        if (!StringUtils.hasText(value)) {
            return hasCoordinates
                    ? DISTANCE
                    : ACCURACY;
        }

        String normalizedValue =
                value.trim()
                        .toLowerCase();

        for (PlaceSearchSort sort : values()) {
            if (sort.requestValue.equals(
                    normalizedValue
            )) {
                return sort;
            }
        }

        throw new CustomException(
                ErrorCode.INVALID_REQUEST
        );
    }
}
