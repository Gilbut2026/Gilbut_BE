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

    public List<EmergencyContactResponse> getEmergencyContacts(
            Long userId
    ) {
        return emergencyContactRepository
                .findAllByUserIdOrderByPriorityAscIdAsc(userId)
                .stream()
                .map(EmergencyContactResponse::from)
                .toList();
    }

    @Transactional
    public EmergencyContactResponse createEmergencyContact(
            Long userId,
            EmergencyContactSaveRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        List<EmergencyContact> contacts = emergencyContactRepository
                .findAllByUserIdOrderByPriorityAscIdAsc(userId);

        normalizePriorities(contacts);

        EmergencyContact contact = EmergencyContact.builder()
                .user(user)
                .name(request.getName().trim())
                .relationship(request.getRelationship().trim())
                .phoneNumber(normalizePhoneNumber(request.getPhoneNumber()))
                .priority(contacts.size() + 1)
                .build();

        EmergencyContact savedContact =
                emergencyContactRepository.save(contact);

        return EmergencyContactResponse.from(savedContact);
    }

    @Transactional
    public EmergencyContactResponse updateEmergencyContact(
            Long userId,
            Long contactId,
            EmergencyContactSaveRequest request
    ) {
        EmergencyContact contact = emergencyContactRepository
                .findByIdAndUserId(contactId, userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.EMERGENCY_CONTACT_NOT_FOUND
                        )
                );

        contact.update(
                request.getName().trim(),
                request.getRelationship().trim(),
                normalizePhoneNumber(request.getPhoneNumber())
        );

        return EmergencyContactResponse.from(contact);
    }

    @Transactional
    public void deleteEmergencyContact(
            Long userId,
            Long contactId
    ) {
        EmergencyContact contact = emergencyContactRepository
                .findByIdAndUserId(contactId, userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.EMERGENCY_CONTACT_NOT_FOUND
                        )
                );

        List<EmergencyContact> contacts = emergencyContactRepository
                .findAllByUserIdOrderByPriorityAscIdAsc(userId);

        emergencyContactRepository.delete(contact);

        int priority = 1;

        for (EmergencyContact existingContact : contacts) {
            if (!contactId.equals(existingContact.getId())) {
                existingContact.updatePriority(priority++);
            }
        }
    }

    private void normalizePriorities(
            List<EmergencyContact> contacts
    ) {
        for (int i = 0; i < contacts.size(); i++) {
            contacts.get(i).updatePriority(i + 1);
        }
    }

    private String normalizePhoneNumber(
            String phoneNumber
    ) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}