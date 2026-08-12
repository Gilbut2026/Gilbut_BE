package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.dto.route.walking.request.NavigationRerouteRequest;
import com.gilbeot.gilbut.dto.route.walking.response.WalkingRouteResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalkingRerouteService {

    private final WalkingRouteService walkingRouteService;

    public WalkingRouteResponse reroute(
            NavigationRerouteRequest request
    ) {
        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        return walkingRouteService.search(
                request.toWalkingRouteRequest()
        );
    }
}
