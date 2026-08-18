package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.ChatSessionRepository;
import com.gilbeot.gilbut.repository.EmergencyContactRepository;
import com.gilbeot.gilbut.repository.FavoritePlaceRepository;
import com.gilbeot.gilbut.repository.HomePlaceRepository;
import com.gilbeot.gilbut.repository.RouteHistoryRepository;
import com.gilbeot.gilbut.repository.UserAccessibilitySettingRepository;
import com.gilbeot.gilbut.repository.UserMobilityProfileRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 탈퇴 — 계정과 사용자에게 딸린 자료를 모두 지운다.
 *
 * 이 앱에는 집 주소, 비상 연락처, 언제 어디를 다녔는지가 남는다.
 * 그만 쓰겠다는 사람이 그것을 지울 방법은 있어야 한다.
 *
 * users 를 참조하는 테이블이 일곱이고 모두 user_id 외래키가 걸려 있어,
 * 자식을 먼저 지우고 users 를 마지막에 지운다. 한 트랜잭션이라
 * 중간에 실패하면 아무것도 지워지지 않는다 — 절반만 지워진 계정은 남지 않는다.
 */
@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final HomePlaceRepository homePlaceRepository;
    private final RouteHistoryRepository routeHistoryRepository;
    private final UserAccessibilitySettingRepository accessibilitySettingRepository;
    private final UserMobilityProfileRepository mobilityProfileRepository;

    @Transactional
    public void withdraw(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        chatSessionRepository.deleteByUserId(userId);
        emergencyContactRepository.deleteByUserId(userId);
        favoritePlaceRepository.deleteByUserId(userId);
        homePlaceRepository.deleteByUserId(userId);
        routeHistoryRepository.deleteByUserId(userId);
        accessibilitySettingRepository.deleteByUserId(userId);
        mobilityProfileRepository.deleteByUserId(userId);

        // 자식 삭제를 먼저 DB 로 내보낸다.
        // 안 그러면 users 삭제가 같은 flush 에 섞여 외래키 제약에 걸릴 수 있다.
        userRepository.flush();

        userRepository.delete(user);
    }
}
