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
}
