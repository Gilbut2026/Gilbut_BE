package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.chat.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findByUserId(Long userId);
}