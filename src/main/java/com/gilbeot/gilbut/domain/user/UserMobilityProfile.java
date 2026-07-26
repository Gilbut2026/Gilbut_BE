package com.gilbeot.gilbut.domain.user;

import com.gilbeot.gilbut.domain.user.type.MobilityAid;
import com.gilbeot.gilbut.domain.user.type.RestStopPreference;
import com.gilbeot.gilbut.domain.user.type.StairLevel;
import com.gilbeot.gilbut.domain.user.type.TransferLevel;
import com.gilbeot.gilbut.domain.user.type.WalkingDuration;
import com.gilbeot.gilbut.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_mobility_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_mobility_profiles_user_id",
                        columnNames = "user_id"
                )
        }
)
public class UserMobilityProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "walking_duration", nullable = false, length = 30)
    private WalkingDuration walkingDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "stair_level", nullable = false, length = 30)
    private StairLevel stairLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "rest_stop_preference", nullable = false, length = 30)
    private RestStopPreference restStopPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_level", nullable = false, length = 30)
    private TransferLevel transferLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "mobility_aid", nullable = false, length = 30)
    private MobilityAid mobilityAid;

    @Column(name = "mobility_aid_detail", length = 100)
    private String mobilityAidDetail;

    public void update(
            WalkingDuration walkingDuration,
            StairLevel stairLevel,
            RestStopPreference restStopPreference,
            TransferLevel transferLevel,
            MobilityAid mobilityAid,
            String mobilityAidDetail
    ) {
        this.walkingDuration = walkingDuration;
        this.stairLevel = stairLevel;
        this.restStopPreference = restStopPreference;
        this.transferLevel = transferLevel;
        this.mobilityAid = mobilityAid;
        this.mobilityAidDetail =
                mobilityAid == MobilityAid.OTHER
                        ? mobilityAidDetail
                        : null;
    }
}