package com.gilbeot.gilbut.dto.place.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FavoritePlaceUpdateRequest {

    @NotBlank(message = "장소명을 입력해 주세요.")
    @Size(
            max = 100,
            message = "장소명은 100자 이하이어야 합니다."
    )
    private String name;
}
