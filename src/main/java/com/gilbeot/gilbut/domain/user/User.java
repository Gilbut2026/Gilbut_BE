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
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_username",
                        columnNames = "username"
                ),
                @UniqueConstraint(
                        name = "uk_users_provider_id",
                        columnNames = "provider_id"
                )
        }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "provider_id", nullable = false, unique = true, length = 100)
    private String providerId;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateUsername(String username) {
        if (username != null && !username.isBlank()) {
            this.username = username;
        }
    }
}