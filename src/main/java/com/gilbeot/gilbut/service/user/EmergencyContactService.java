package com.gilbeot.gilbut.service.user;

import com.gilbeot.gilbut.domain.user.EmergencyContact;
import com.gilbeot.gilbut.domain.user.User;
import com.gilbeot.gilbut.dto.user.request.EmergencyContactSaveRequest;
import com.gilbeot.gilbut.dto.user.response.EmergencyContactResponse;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.repository.EmergencyContactRepository;
import com.gilbeot.gilbut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmergencyContactService {

    private final UserRepository userRepository;
    private final EmergencyContactRepository emergencyContactRepository;

    // 비상 연락처 목록 조회
    public List<EmergencyContactResponse> getEmergencyContacts(
            Long userId
    ) {
        return emergencyContactRepository
                .findAllByUserIdOrderByPriorityAscIdAsc(userId)
                .stream()
                .map(EmergencyContactResponse::from)
                .toList();
    }

    // 비상 연락처 등록
    @Transactional
    public EmergencyContactResponse createEmergencyContact(
            Long userId,
            EmergencyContactSaveRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        EmergencyContact contact =
                EmergencyContact.builder()
                        .user(user)
                        .name(request.getName().trim())
                        .relationship(
                                request.getRelationship().trim()
                        )
                        .phoneNumber(
                                normalizePhoneNumber(
                                        request.getPhoneNumber()
                                )
                        )
                        .priority(request.getPriority())
                        .build();

        EmergencyContact savedContact =
                emergencyContactRepository.save(contact);

        return EmergencyContactResponse.from(savedContact);
    }

    // 비상 연락처 수정
    @Transactional
    public EmergencyContactResponse updateEmergencyContact(
            Long userId,
            Long contactId,
            EmergencyContactSaveRequest request
    ) {
        EmergencyContact contact =
                emergencyContactRepository
                        .findByIdAndUserId(contactId, userId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.EMERGENCY_CONTACT_NOT_FOUND
                                )
                        );

        contact.update(
                request.getName().trim(),
                request.getRelationship().trim(),
                normalizePhoneNumber(request.getPhoneNumber()),
                request.getPriority()
        );

        EmergencyContact savedContact =
                emergencyContactRepository.save(contact);

        return EmergencyContactResponse.from(savedContact);
    }

    // 비상 연락처 삭제
    @Transactional
    public void deleteEmergencyContact(
            Long userId,
            Long contactId
    ) {
        EmergencyContact contact =
                emergencyContactRepository
                        .findByIdAndUserId(contactId, userId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.EMERGENCY_CONTACT_NOT_FOUND
                                )
                        );

        emergencyContactRepository.delete(contact);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}