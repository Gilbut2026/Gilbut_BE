package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.favorite.FavoritePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritePlaceRepository
        extends JpaRepository<FavoritePlace, Long> {

    List<FavoritePlace> findAllByUserIdOrderByIdDesc(Long userId);

    Optional<FavoritePlace> findByIdAndUserId(
            Long id,
            Long userId
    );

    // 회원 탈퇴 시 사용자에게 딸린 것을 한 번에 지운다
    void deleteByUserId(Long userId);
}
