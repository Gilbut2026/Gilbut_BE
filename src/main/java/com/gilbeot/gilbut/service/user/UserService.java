package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.user.response.UserResponse;
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
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }

    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        return UserResponse.from(user);
    }

    // 카카오 닉네임 없으면 임의의 username 생성

    @Transactional
    public User createUser(String providerId, String nickname) {
        String username = nickname != null && !nickname.isBlank()
                ? resolveAvailableUsername(nickname)
                : generateUniqueUsername();

        User user = User.builder()
                .providerId(providerId)
                .username(username)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void updateRefreshToken(Long userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        user.updateRefreshToken(refreshToken);
    }

    @Transactional
    public void clearRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        user.updateRefreshToken(null);
    }

    private String resolveAvailableUsername(String nickname) {
        if (!userRepository.existsByUsername(nickname)) {
            return nickname;
        }

        String username;

        do {
            String suffix = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 4);

            username = nickname + "_" + suffix;
        } while (userRepository.existsByUsername(username));

        return username;
    }

    private String generateUniqueUsername() {
        String username;

        do {
            username = generateRandomUsername();
        } while (userRepository.existsByUsername(username));

        return username;
    }

    private String generateRandomUsername() {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        return "gilbut_" + randomPart;
    }
}