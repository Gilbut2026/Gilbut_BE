package com.gilbeot.gilbut.dto.route;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransitRouteFailureCode {

    NO_ROUTE(
            "조회 가능한 대중교통 노선이 없습니다."
    ),

    QUOTA_EXCEEDED(
            "대중교통 경로 조회 한도를 초과했습니다."
    ),

    KEY_OR_PERMISSION(
            "대중교통 API 키 또는 권한을 확인해야 합니다."
    ),

    PROVIDER_ERROR(
            "대중교통 경로 조회에 실패했습니다."
    );

    private final String message;
}
