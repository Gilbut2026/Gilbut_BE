package com.gilbeot.gilbut.dto.route;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RouteCandidateResult {

    private String requestId;
    private List<RouteCandidate> candidates;
}