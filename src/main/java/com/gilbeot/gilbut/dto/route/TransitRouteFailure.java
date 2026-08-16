package com.gilbeot.gilbut.dto.route;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransitRouteFailure {

    private TransitRouteFailureCode code;
    private String message;

    public static TransitRouteFailure from(
            TransitRouteFailureCode code
    ) {
        TransitRouteFailureCode resolvedCode =
                code == null
                        ? TransitRouteFailureCode.PROVIDER_ERROR
                        : code;

        return TransitRouteFailure.builder()
                .code(resolvedCode)
                .message(
                        resolvedCode.getMessage()
                )
                .build();
    }
}
