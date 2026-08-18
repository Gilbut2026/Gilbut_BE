package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.user.UserMobilityProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMobilityProfileRepository
        extends JpaRepository<UserMobilityProfile, Long> {

    Optional<UserMobilityProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    // 회원 탈퇴 시 사용자에게 딸린 것을 한 번에 지운다
    void deleteByUserId(Long userId);
}
