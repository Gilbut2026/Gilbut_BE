package com.gilbeot.gilbut.domain.chat;

public enum ChatState {

    // 목적지를 입력받고 장소 검색 및 확정을 기다리는 상태
    DESTINATION_WAITING,

    // 목적지 확정 후 출발지 확인을 기다리는 상태
    ORIGIN_CONFIRMATION,

    // 출발지와 목적지를 기준으로 경로를 계산하는 상태
    ROUTE_CALCULATING,

    // 계산된 경로 결과를 사용자에게 제시하는 상태
    RESULT_PRESENTATION,

    // 사용자가 선택한 경로로 이동 안내 중인 상태
    NAVIGATING
}