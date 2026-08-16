package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.EmergencyContact;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.user.request.EmergencyContactSaveRequest;
import com.gilbeot.gilbut.dto.user.response.EmergencyContactResponse;
import com.gilbeot.gilbut.repository.EmergencyContactRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyContactServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    private EmergencyContactService emergencyContactService;

    @BeforeEach
    void setUp() {
        emergencyContactService = new EmergencyContactService(
                userRepository,
                emergencyContactRepository
        );
    }

    @Test
    @DisplayName("비상연락처 등록 시 기존 연락처 다음 순위가 자동으로 지정된다")
    void createEmergencyContactAssignsNextPriority() {
        Long userId = 1L;
        User user = user(userId);

        EmergencyContact first =
                contact(1L, user, 1);

        EmergencyContact second =
                contact(2L, user, 2);

        EmergencyContactSaveRequest request =
                request(
                        "김길벗",
                        "보호자",
                        "010-1234-5678"
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(
                emergencyContactRepository
                        .findAllByUserIdOrderByPriorityAscIdAsc(userId)
        ).thenReturn(
                List.of(first, second)
        );

        when(
                emergencyContactRepository.save(
                        any(EmergencyContact.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        EmergencyContactResponse result =
                emergencyContactService
                        .createEmergencyContact(
                                userId,
                                request
                        );

        assertThat(result.getPriority())
                .isEqualTo(3);

        assertThat(result.getPhoneNumber())
                .isEqualTo("01012345678");
    }

    @Test
    @DisplayName("기존 우선순위에 빈칸이 있어도 등록 전에 순서를 정규화한다")
    void createEmergencyContactNormalizesExistingPriorities() {
        Long userId = 1L;
        User user = user(userId);

        EmergencyContact first =
                contact(1L, user, 1);

        EmergencyContact second =
                contact(2L, user, 3);

        EmergencyContactSaveRequest request =
                request(
                        "김길벗",
                        "보호자",
                        "01012345678"
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(
                emergencyContactRepository
                        .findAllByUserIdOrderByPriorityAscIdAsc(userId)
        ).thenReturn(
                List.of(first, second)
        );

        when(
                emergencyContactRepository.save(
                        any(EmergencyContact.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        EmergencyContactResponse result =
                emergencyContactService
                        .createEmergencyContact(
                                userId,
                                request
                        );

        assertThat(first.getPriority())
                .isEqualTo(1);

        assertThat(second.getPriority())
                .isEqualTo(2);

        assertThat(result.getPriority())
                .isEqualTo(3);
    }

    @Test
    @DisplayName("비상연락처 수정 시 기존 우선순위는 유지된다")
    void updateEmergencyContactKeepsPriority() {
        Long userId = 1L;
        Long contactId = 10L;
        User user = user(userId);

        EmergencyContact contact =
                contact(
                        contactId,
                        user,
                        2
                );

        EmergencyContactSaveRequest request =
                request(
                        "수정된 이름",
                        "가족",
                        "010-9999-8888"
                );

        when(
                emergencyContactRepository
                        .findByIdAndUserId(
                                contactId,
                                userId
                        )
        ).thenReturn(
                Optional.of(contact)
        );

        EmergencyContactResponse result =
                emergencyContactService
                        .updateEmergencyContact(
                                userId,
                                contactId,
                                request
                        );

        assertThat(result.getName())
                .isEqualTo("수정된 이름");

        assertThat(result.getRelationship())
                .isEqualTo("가족");

        assertThat(result.getPhoneNumber())
                .isEqualTo("01099998888");

        assertThat(result.getPriority())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("중간 순위 연락처 삭제 시 남은 연락처 우선순위를 다시 정렬한다")
    void deleteEmergencyContactReordersPriorities() {
        Long userId = 1L;
        Long contactId = 2L;
        User user = user(userId);

        EmergencyContact first =
                contact(1L, user, 1);

        EmergencyContact second =
                contact(contactId, user, 2);

        EmergencyContact third =
                contact(3L, user, 3);

        when(
                emergencyContactRepository
                        .findByIdAndUserId(
                                contactId,
                                userId
                        )
        ).thenReturn(
                Optional.of(second)
        );

        when(
                emergencyContactRepository
                        .findAllByUserIdOrderByPriorityAscIdAsc(userId)
        ).thenReturn(
                List.of(
                        first,
                        second,
                        third
                )
        );

        emergencyContactService
                .deleteEmergencyContact(
                        userId,
                        contactId
                );

        verify(emergencyContactRepository)
                .delete(second);

        assertThat(first.getPriority())
                .isEqualTo(1);

        assertThat(third.getPriority())
                .isEqualTo(2);
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .username("tester")
                .providerId("provider-" + id)
                .build();
    }

    private EmergencyContact contact(
            Long id,
            User user,
            Integer priority
    ) {
        return EmergencyContact.builder()
                .id(id)
                .user(user)
                .name("연락처" + id)
                .relationship("가족")
                .phoneNumber("01012345678")
                .priority(priority)
                .build();
    }

    private EmergencyContactSaveRequest request(
            String name,
            String relationship,
            String phoneNumber
    ) {
        EmergencyContactSaveRequest request =
                mock(EmergencyContactSaveRequest.class);

        when(request.getName())
                .thenReturn(name);

        when(request.getRelationship())
                .thenReturn(relationship);

        when(request.getPhoneNumber())
                .thenReturn(phoneNumber);

        return request;
    }
}