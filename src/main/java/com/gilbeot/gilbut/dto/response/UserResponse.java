package com.gilbeot.gilbut.dto.response;

import com.gilbeot.gilbut.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String avatarUrl;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getAvatarUrl());
    }
}
