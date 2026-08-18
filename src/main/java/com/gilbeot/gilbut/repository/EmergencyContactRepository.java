package com.gilbeot.gilbut.repository;

import com.gilbeot.gilbut.domain.user.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactRepository
        extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findAllByUserIdOrderByPriorityAscIdAsc(
            Long userId
    );

    Optional<EmergencyContact> findByIdAndUserId(
            Long id,
            Long userId
    );
    long countByUserId(Long userId);

    // 회원 탈퇴 시 사용자에게 딸린 것을 한 번에 지운다
    void deleteByUserId(Long userId);
}
