package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.home.HomePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomePlaceRepository
        extends JpaRepository<HomePlace, Long> {

    Optional<HomePlace> findByUserId(Long userId);
}