package com.gilbeot.gilbut.service.route;

import com.gilbeot.gilbut.dto.route.RouteCandidate;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import com.gilbeot.gilbut.dto.route.RouteCandidateResult;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteCandidateAggregationService {

    private final RouteCandidateService routeCandidateService;
    private final WalkingRouteCandidateService walkingRouteCandidateService;

    public RouteCandidateResult createCandidates(
            RouteCandidateRequest request
    ) {
        List<RouteCandidate> candidates = new ArrayList<>();

        addWalkingCandidate(request, candidates);
        addTransitCandidates(request, candidates);

        if (candidates.isEmpty()) {
            throw new CustomException(ErrorCode.ROUTE_SEARCH_FAILED);
        }

        return RouteCandidateResult.builder()
                .requestId(UUID.randomUUID().toString())
                .candidates(candidates)
                .build();
    }

    private void addWalkingCandidate(
            RouteCandidateRequest request,
            List<RouteCandidate> candidates
    ) {
        try {
            RouteCandidate walkingCandidate =
                    walkingRouteCandidateService.createCandidate(request);

            candidates.add(walkingCandidate);
        } catch (CustomException e) {
            if (e.getErrorCode() != ErrorCode.ROUTE_SEARCH_FAILED) {
                throw e;
            }
        }
    }

    private void addTransitCandidates(
            RouteCandidateRequest request,
            List<RouteCandidate> candidates
    ) {
        try {
            RouteCandidateResult transitResult =
                    routeCandidateService.createCandidates(request);

            if (transitResult.getCandidates() != null) {
                candidates.addAll(transitResult.getCandidates());
            }
        } catch (CustomException e) {
            if (e.getErrorCode() != ErrorCode.ROUTE_SEARCH_FAILED) {
                throw e;
            }
        }
    }
}