package com.gilbeot.gilbut.service.chat;

import com.gilbeot.gilbut.domain.chat.ChatSession;
import com.gilbeot.gilbut.domain.chat.ChatState;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.chat.request.PlaceConfirmationRequest;
import com.gilbeot.gilbut.dto.chat.response.ChatSessionResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.ChatSessionRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatSessionService {

    private final UserRepository userRepository;

    private final ChatSessionRepository chatSessionRepository;

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
}