package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.user.UserAccessibilitySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccessibilitySettingRepository
        extends JpaRepository<UserAccessibilitySetting, Long> {
    Optional<UserAccessibilitySetting> findByUserId(Long userId);
}