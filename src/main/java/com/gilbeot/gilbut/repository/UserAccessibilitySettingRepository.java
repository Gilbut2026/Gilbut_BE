package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.user.UserAccessibilitySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccessibilitySettingRepository
        extends JpaRepository<UserAccessibilitySetting, Long> {
    Optional<UserAccessibilitySetting> findByUserId(Long userId);

    // 회원 탈퇴 시 사용자에게 딸린 것을 한 번에 지운다
    void deleteByUserId(Long userId);
}
