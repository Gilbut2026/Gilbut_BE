package com.gilbeot.gilbut.domain.chat;

import com.gilbeot.gilbut.domain.user.User;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_sessions_user_id",
                        columnNames = "user_id"
                ),
                @UniqueConstraint(
                        name = "uk_chat_sessions_session_id",
                        columnNames = "session_id"
                )
        }
)
public class ChatSession extends BaseEntity {

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

    @Column(
            name = "session_id",
            nullable = false,
            unique = true,
            length = 36
    )
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "current_state",
            nullable = false,
            length = 40,
            columnDefinition = "VARCHAR(40)"
    )
    private ChatState currentState;

    @Column(
            name = "destination_place_id",
            length = 100
    )
    private String destinationPlaceId;

    @Column(
            name = "destination_name",
            length = 100
    )
    private String destinationName;

    @Column(
            name = "destination_address",
            length = 255
    )
    private String destinationAddress;

    @Column(name = "destination_latitude")
    private Double destinationLatitude;

    @Column(name = "destination_longitude")
    private Double destinationLongitude;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "origin_type",
            length = 30
    )
    private OriginType originType;

    @Column(
            name = "origin_place_id",
            length = 100
    )
    private String originPlaceId;

    @Column(
            name = "origin_name",
            length = 100
    )
    private String originName;

    @Column(
            name = "origin_address",
            length = 255
    )
    private String originAddress;

    @Column(name = "origin_latitude")
    private Double originLatitude;

    @Column(name = "origin_longitude")
    private Double originLongitude;

    @Column(
            name = "selected_route_id",
            length = 100
    )
    private String selectedRouteId;

    @Column(
            name = "active_request_id",
            length = 36
    )
    private String activeRequestId;

    @Column(name = "departure_datetime")
    private LocalDateTime departureDateTime;

    public static ChatSession create(User user) {
        return ChatSession.builder()
                .user(user)
                .sessionId(UUID.randomUUID().toString())
                .currentState(ChatState.DESTINATION_WAITING)
                .build();
    }

    public void reset() {
        this.sessionId = UUID.randomUUID().toString();
        this.currentState = ChatState.DESTINATION_WAITING;

        clearDestination();
        clearOrigin();
        clearRouteContext();
    }

    public void confirmDestination(
            String placeId,
            String name,
            String address,
            Double latitude,
            Double longitude
    ) {
        this.destinationPlaceId = placeId;
        this.destinationName = name;
        this.destinationAddress = address;
        this.destinationLatitude = latitude;
        this.destinationLongitude = longitude;
        this.currentState = ChatState.ORIGIN_CONFIRMATION;

        clearOrigin();
        clearRouteContext();
    }

    public void confirmOrigin(
            OriginType originType,
            String placeId,
            String name,
            String address,
            Double latitude,
            Double longitude
    ) {
        this.originType = originType;
        this.originPlaceId = placeId;
        this.originName = name;
        this.originAddress = address;
        this.originLatitude = latitude;
        this.originLongitude = longitude;
    }

    public void startRouteCalculation(String requestId) {
        this.activeRequestId = requestId;
        this.selectedRouteId = null;
        this.currentState = ChatState.ROUTE_CALCULATING;
    }

    public void completeRouteCalculation() {
        this.currentState = ChatState.RESULT_PRESENTATION;
    }

    public void moveToDepartureTimeConfirmation() {
        this.currentState =
                ChatState.DEPARTURE_TIME_CONFIRMATION;
    }

    public void startNavigation(String routeId) {
        this.selectedRouteId = routeId;
        this.currentState = ChatState.NAVIGATING;
    }

    public void moveToDestinationWaiting() {
        this.currentState = ChatState.DESTINATION_WAITING;

        clearDestination();
        clearOrigin();
        clearRouteContext();
    }

    public void moveToOriginConfirmation() {
        this.currentState = ChatState.ORIGIN_CONFIRMATION;

        clearOrigin();
        clearRouteContext();
    }

    private void clearDestination() {
        this.destinationPlaceId = null;
        this.destinationName = null;
        this.destinationAddress = null;
        this.destinationLatitude = null;
        this.destinationLongitude = null;
    }

    private void clearOrigin() {
        this.originType = null;
        this.originPlaceId = null;
        this.originName = null;
        this.originAddress = null;
        this.originLatitude = null;
        this.originLongitude = null;
    }

    private void clearRouteContext() {
        this.departureDateTime = null;
        this.selectedRouteId = null;
        this.activeRequestId = null;
    }

    public void confirmDepartureTime(
            LocalDateTime departureDateTime
    ) {
        this.departureDateTime = departureDateTime;
        this.currentState =
                ChatState.TODAY_CONDITION_CONFIRMATION;
    }
}