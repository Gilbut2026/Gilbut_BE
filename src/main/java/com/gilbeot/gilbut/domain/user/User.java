package com.gilbeot.gilbut.domain.user;

import com.gilbeot.gilbut.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = {"provider", "providerId"})
        }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String avatarUrl;

    @Column(nullable = false, length = 20)
    private String provider;      // kakao

    @Column(nullable = false, unique = true, length = 100)
    private String providerId;    // 카카오 회원 고유 id

    // 카카오 로그인 시 재발급받은 refresh token 저장 (Redis 미사용, DB 컬럼 방식)
    @Column(length = 255)
    private String refreshToken;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void update(String username, String avatarUrl) {
        if (username != null) this.username = username;
        if (avatarUrl != null) this.avatarUrl = avatarUrl;
    }
}
