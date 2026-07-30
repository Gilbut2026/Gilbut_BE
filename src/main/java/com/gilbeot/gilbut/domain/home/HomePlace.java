package com.gilbeot.gilbut.domain.home;

import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "home_places",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_home_places_user_id",
                        columnNames = "user_id"
                )
        }
)
public class HomePlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    public void update(
            String address,
            Double latitude,
            Double longitude
    ) {
        this.address = address.trim();
        this.latitude = latitude;
        this.longitude = longitude;
    }
}