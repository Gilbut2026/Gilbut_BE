package com.gilbeot.gilbut.global.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode {

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "잘못된 요청입니다."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "사용자를 찾을 수 없습니다."
    ),

    MOBILITY_PROFILE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "사용자 이동 특성 정보를 찾을 수 없습니다."
    ),

    MOBILITY_AID_DETAIL_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "기타 보조기구를 선택한 경우 상세 내용을 입력해야 합니다."
    ),

    EMERGENCY_CONTACT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "비상 연락처를 찾을 수 없습니다."
    ),

    FAVORITE_PLACE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "즐겨찾기 장소를 찾을 수 없습니다."
    ),

    HOME_PLACE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "집 주소를 찾을 수 없습니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    ),

    JWT_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "토큰이 유효하지 않거나 만료되었습니다."
    ),

    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "접근 권한이 없습니다."
    ),

    OAUTH_FAILED(
            HttpStatus.UNAUTHORIZED,
            "OAuth 인증에 실패했습니다."
    ),

    PLACE_SEARCH_FAILED(
            HttpStatus.BAD_GATEWAY,
            "장소 검색에 실패했습니다."
    );

    private final HttpStatus status;
    private final String message;
}
