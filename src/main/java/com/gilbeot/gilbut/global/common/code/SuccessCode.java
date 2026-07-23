package com.gilbeot.gilbut.global.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode implements BaseCode {
    _OK(HttpStatus.OK, "성공"),
    _CREATED(HttpStatus.CREATED, "생성 완료"),
    _NO_CONTENT(HttpStatus.NO_CONTENT, "처리 완료");

    private final HttpStatus status;
    private final String message;
}
