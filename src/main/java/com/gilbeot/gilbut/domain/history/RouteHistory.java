package com.gilbeot.gilbut.domain.history;

import com.gilbeot.gilbut.domain.route.RouteType;
import com.gilbeot.gilbut.domain.route.WalkingRouteOption;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.drt.DrtServiceArea;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_histories")
public class RouteHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "request_id", nullable = false, length = 36)
    private String requestId;

    @Column(name = "origin_name", nullable = false, length = 100)
    private String originName;

    @Column(name = "origin_address", length = 255)
    private String originAddress;

    @Column(name = "origin_latitude", nullable = false)
    private Double originLatitude;

    @Column(name = "origin_longitude", nullable = false)
    private Double originLongitude;

    @Column(name = "destination_name", nullable = false, length = 100)
    private String destinationName;

    @Column(name = "destination_address", length = 255)
    private String destinationAddress;

    @Column(name = "destination_latitude", nullable = false)
    private Double destinationLatitude;

    @Column(name = "destination_longitude", nullable = false)
    private Double destinationLongitude;

    @Column(name = "departure_datetime")
    private LocalDateTime departureDateTime;

    @Column(name = "recommended_route_id", length = 100)
    private String recommendedRouteId;

    @Column(name = "recommendation_reason", length = 500)
    private String recommendationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_route_type", length = 20)
    private RouteType recommendedRouteType;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_route_option", length = 30)
    private WalkingRouteOption recommendedRouteOption;

    @Column(name = "total_time_sec")
    private Integer totalTimeSec;

    @Column(name = "total_walk_time_sec")
    private Integer totalWalkTimeSec;

    @Column(name = "total_walk_distance_m")
    private Integer totalWalkDistanceM;

    @Column(name = "transfer_count")
    private Integer transferCount;

    @Column(name = "drt_recommended", nullable = false)
    private boolean drtRecommended;

    @Enumerated(EnumType.STRING)
    @Column(name = "drt_service_area", length = 50)
    private DrtServiceArea drtServiceArea;
}