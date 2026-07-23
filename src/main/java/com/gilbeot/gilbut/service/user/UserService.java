package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.response.UserResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    public UserResponse getUserInfo(Long userId) {
        return UserResponse.from(userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
    }

    // 신규 가입 사용자 생성
    // 닉네임 없으면 랜덤 username으로 대체
    public User createUser(String provider, String providerId, String avatarUrl, String nickname) {
        String username = (nickname != null && !nickname.isBlank())
                ? resolveAvailableUsername(nickname)
                : generateUniqueUsername();

        User user = User.builder()
                .provider(provider)
                .providerId(providerId)
                .username(username)
                .avatarUrl(avatarUrl)
                .build();

        return userRepository.save(user);
    }

    private String resolveAvailableUsername(String nickname) {
        if (!userRepository.existsByUsername(nickname)) {
            return nickname;
        }
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return nickname + "_" + suffix;
    }

    private String generateRandomUsername() {
        String prefix = "gilbut_";
        String randomPart = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8);
        return prefix + randomPart;
    }

    private String generateUniqueUsername() {
        String username;
        do {
            username = generateRandomUsername();
        } while (userRepository.existsByUsername(username));
        return username;
    }
}
