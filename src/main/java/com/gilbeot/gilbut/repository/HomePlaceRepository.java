package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.home.HomePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomePlaceRepository
        extends JpaRepository<HomePlace, Long> {

    Optional<HomePlace> findByUserId(Long userId);

    // 회원 탈퇴 시 사용자에게 딸린 것을 한 번에 지운다
    void deleteByUserId(Long userId);
}
