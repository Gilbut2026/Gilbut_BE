package com.gilbeot.gilbut.domain.user;

import com.gilbeot.gilbut.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "emergency_contacts")
public class EmergencyContact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 연락처 이름
    @Column(nullable = false, length = 30)
    private String name;

    // 관계
    @Column(nullable = false, length = 30)
    private String relationship;

    // 전화번호
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    // SOS 연락 우선순위
    @Column(nullable = false)
    private Integer priority;

    public void update(
            String name,
            String relationship,
            String phoneNumber,
            Integer priority
    ) {
        this.name = name;
        this.relationship = relationship;
        this.phoneNumber = phoneNumber;
        this.priority = priority;
    }

    public void updatePriority(Integer priority) {
        this.priority = priority;
    }
}