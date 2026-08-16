package com.gilbeot.gilbut.global.exception;

import com.gilbeot.gilbut.dto.route.TransitRouteFailureCode;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import lombok.Getter;

@Getter
public class TransitRouteSearchException extends CustomException {

    private final TransitRouteFailureCode failureCode;

    public TransitRouteSearchException(
            TransitRouteFailureCode failureCode
    ) {
        super(ErrorCode.ROUTE_SEARCH_FAILED);
        this.failureCode =
                failureCode == null
                        ? TransitRouteFailureCode.PROVIDER_ERROR
                        : failureCode;
    }
}
