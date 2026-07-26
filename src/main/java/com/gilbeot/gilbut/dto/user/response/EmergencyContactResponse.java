package com.gilbeot.gilbut.dto.user.response;

import com.gilbeot.gilbut.domain.user.EmergencyContact;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmergencyContactResponse {

    private Long id;

    private String name;

    private String relationship;

    private String phoneNumber;

    private Integer priority;

    public static EmergencyContactResponse from(
            EmergencyContact contact
    ) {
        return EmergencyContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .relationship(contact.getRelationship())
                .phoneNumber(contact.getPhoneNumber())
                .priority(contact.getPriority())
                .build();
    }
}