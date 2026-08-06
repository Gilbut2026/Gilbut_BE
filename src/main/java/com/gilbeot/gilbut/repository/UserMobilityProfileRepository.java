package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.user.UserMobilityProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMobilityProfileRepository
        extends JpaRepository<UserMobilityProfile, Long> {

    Optional<UserMobilityProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}