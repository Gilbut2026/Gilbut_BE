package com.gilbeot.gilbut.dto.chat.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    @NotBlank(message = "메시지를 입력해 주세요.")
    @Size(
            max = 500,
            message = "메시지는 500자 이하로 입력해 주세요."
    )
    private String message;
}