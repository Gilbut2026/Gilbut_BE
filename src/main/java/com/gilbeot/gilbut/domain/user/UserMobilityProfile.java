package com.gilbeot.gilbut.domain.user;

import com.gilbeot.gilbut.domain.user.type.MobilityAid;
import com.gilbeot.gilbut.domain.user.type.RestStopPreference;
import com.gilbeot.gilbut.domain.user.type.SlopeLevel;
import com.gilbeot.gilbut.domain.user.type.StairLevel;
import com.gilbeot.gilbut.domain.user.type.TransferLevel;
import com.gilbeot.gilbut.domain.user.type.WalkingDuration;
import com.gilbeot.gilbut.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
    @Column(
            name = "walking_duration",
            nullable = false,
            length = 30
    )
    private WalkingDuration walkingDuration;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "stair_level",
            nullable = false,
            length = 30
    )
    private StairLevel stairLevel;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "slope_level",
            nullable = false,
            length = 30
    )
    private SlopeLevel slopeLevel;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "rest_stop_preference",
            nullable = false,
            length = 30
    )
    private RestStopPreference restStopPreference;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "transfer_level",
            nullable = false,
            length = 30
    )
    private TransferLevel transferLevel;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "mobility_aid",
            nullable = false,
            length = 30
    )
    private MobilityAid mobilityAid;

    public void update(
            WalkingDuration walkingDuration,
            StairLevel stairLevel,
            SlopeLevel slopeLevel,
            RestStopPreference restStopPreference,
            TransferLevel transferLevel,
            MobilityAid mobilityAid
    ) {
        this.walkingDuration = walkingDuration;
        this.stairLevel = stairLevel;
        this.slopeLevel = slopeLevel;
        this.restStopPreference = restStopPreference;
        this.transferLevel = transferLevel;
        this.mobilityAid = mobilityAid;
    }
}