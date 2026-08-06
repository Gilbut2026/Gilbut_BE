package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.repository.UserMobilityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingStatusService {

    private final UserMobilityProfileRepository mobilityProfileRepository;

    public boolean isCompleted(Long userId) {
        return mobilityProfileRepository.existsByUserId(userId);
    }
}