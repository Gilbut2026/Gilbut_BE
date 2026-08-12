package com.gilbeot.gilbut.dto.drt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DrtServiceArea {

    GOSAEK_OMOKCHEON_PYEONGRI("고색·오목천·평리"),
    GWANGGYO("광교1·2동"),
    GWONSEON("권선동"),
    DANGSU("당수동");

    private final String displayName;
}