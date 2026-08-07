package com.gilbeot.gilbut.service.chat;

import com.gilbeot.gilbut.domain.chat.ChatSession;
import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.domain.chat.OriginType;
import com.gilbeot.gilbut.domain.home.HomePlace;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.chat.request.OriginConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.request.PlaceConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.request.TodayConditionConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.request.DepartureTimeConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.response.ChatSessionResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.ChatSessionRepository;
import com.gilbeot.gilbut.repository.HomePlaceRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatSessionService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final HomePlaceRepository homePlaceRepository;

    @Transactional
    public ChatSessionResponse getOrCreateSession(Long userId) {
        ChatSession session =
                getOrCreateSessionEntity(userId);

        return ChatSessionResponse.from(session);
    }

    @Transactional
    public ChatSessionResponse resetSession(Long userId) {
        ChatSession session =
                getOrCreateSessionEntity(userId);

        session.reset();

        ChatSession savedSession =
                chatSessionRepository.save(session);

        return ChatSessionResponse.from(savedSession);
    }

    @Transactional
    public ChatSessionResponse confirmDestination(
            Long userId,
            PlaceConfirmationRequest request
    ) {
        ChatSession session =
                getOrCreateSessionEntity(userId);

        if (session.getCurrentState()
                != ChatState.DESTINATION_WAITING) {

            throw new CustomException(
                    ErrorCode.CHAT_STATE_CONFLICT
            );
        }

        session.confirmDestination(
                request.getPlaceId(),
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );

        return ChatSessionResponse.from(session);
    }

    @Transactional
    public ChatSessionResponse confirmOrigin(
            Long userId,
            OriginConfirmationRequest request
    ) {
        ChatSession session =
                getOrCreateSessionEntity(userId);

        if (session.getCurrentState()
                != ChatState.ORIGIN_CONFIRMATION) {

            throw new CustomException(
                    ErrorCode.CHAT_STATE_CONFLICT
            );
        }

        switch (request.getOriginType()) {

            case CURRENT_LOCATION ->
                    confirmCurrentLocation(
                            session,
                            request
                    );

            case HOME ->
                    confirmHome(
                            userId,
                            session
                    );

            case PLACE ->
                    confirmPlace(
                            session,
                            request
                    );
        }

        session.moveToDepartureTimeConfirmation();

        return ChatSessionResponse.from(session);
    }

    private void confirmCurrentLocation(
            ChatSession session,
            OriginConfirmationRequest request
    ) {
        validateCoordinates(request);

        session.confirmOrigin(
                OriginType.CURRENT_LOCATION,
                null,
                "현재 위치",
                null,
                request.getLatitude(),
                request.getLongitude()
        );
    }

    private void confirmHome(
            Long userId,
            ChatSession session
    ) {
        HomePlace homePlace =
                homePlaceRepository
                        .findByUserId(userId)
                        .orElseThrow(
                                () -> new CustomException(
                                        ErrorCode.HOME_PLACE_NOT_FOUND
                                )
                        );

        session.confirmOrigin(
                OriginType.HOME,
                null,
                "집",
                homePlace.getAddress(),
                homePlace.getLatitude(),
                homePlace.getLongitude()
        );
    }

    private void confirmPlace(
            ChatSession session,
            OriginConfirmationRequest request
    ) {
        if (!StringUtils.hasText(request.getPlaceId())
                || !StringUtils.hasText(request.getName())
                || !StringUtils.hasText(request.getAddress())) {

            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateCoordinates(request);

        session.confirmOrigin(
                OriginType.PLACE,
                request.getPlaceId().trim(),
                request.getName().trim(),
                request.getAddress().trim(),
                request.getLatitude(),
                request.getLongitude()
        );
    }

    private void validateCoordinates(
            OriginConfirmationRequest request
    ) {
        if (request.getLatitude() == null
                || request.getLongitude() == null) {

            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private ChatSession getOrCreateSessionEntity(
            Long userId
    ) {
        return chatSessionRepository
                .findByUserId(userId)
                .orElseGet(
                        () -> createSession(userId)
                );
    }

    private ChatSession createSession(
            Long userId
    ) {
        User user =
                userRepository.findById(userId)
                        .orElseThrow(
                                () -> new CustomException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        ChatSession session =
                ChatSession.create(user);

        return chatSessionRepository.save(
                session
        );
    }

    @Transactional
    public ChatSessionResponse confirmDepartureTime(
            Long userId,
            DepartureTimeConfirmationRequest request
    ) {
        ChatSession session =
                getOrCreateSessionEntity(userId);

        if (session.getCurrentState()
                != ChatState.DEPARTURE_TIME_CONFIRMATION) {

            throw new CustomException(
                    ErrorCode.CHAT_STATE_CONFLICT
            );
        }

        LocalDateTime departureDateTime =
                request.getDepartureDateTime();

        LocalDateTime minimumAllowedTime =
                LocalDateTime.now().minusMinutes(1);

        if (departureDateTime.isBefore(
                minimumAllowedTime
        )) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        session.confirmDepartureTime(
                departureDateTime
        );

        return ChatSessionResponse.from(session);
    }

    @Transactional
    public ChatSessionResponse confirmTodayCondition(
            Long userId,
            TodayConditionConfirmationRequest request
    ) {
        ChatSession session =
                getOrCreateSessionEntity(userId);

        if (session.getCurrentState()
                != ChatState.TODAY_CONDITION_CONFIRMATION) {

            throw new CustomException(
                    ErrorCode.CHAT_STATE_CONFLICT
            );
        }

        session.confirmTodayCondition(
                request.getTodayCondition()
        );

        return ChatSessionResponse.from(session);
    }
}