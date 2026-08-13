package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.history.RouteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteHistoryRepository
        extends JpaRepository<RouteHistory, Long> {

    List<RouteHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<RouteHistory> findByIdAndUserId(
            Long id,
            Long userId
    );
}