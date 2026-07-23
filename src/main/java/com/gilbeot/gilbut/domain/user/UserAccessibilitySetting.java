package com.gilbeot.gilbut.domain.user;

import com.gilbeot.gilbut.domain.user.type.FontSize;
import com.gilbeot.gilbut.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_accessibility_settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_accessibility_settings_user_id",
                        columnNames = "user_id"
                )
        }
)
public class UserAccessibilitySetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 한 명당 접근성 설정 하나
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_user_accessibility_settings_user"
            )
    )
    private User user;

    // 음성으로 읽어줄지 여부

    @Column(
            name = "voice_guidance_enabled",
            nullable = false
    )
    private boolean voiceGuidanceEnabled;

    // 고대비 화면 사용 여부

    @Column(
            name = "high_contrast_enabled",
            nullable = false
    )
    private boolean highContrastEnabled;

    // 화면 글자 크기

    @Enumerated(EnumType.STRING)
    @Column(
            name = "font_size",
            nullable = false,
            length = 20
    )
    private FontSize fontSize;

    /**
     * 음성 안내 재생 속도
     * 권장 범위
     * 0.70 = 느리게
     * 1.00 = 보통
     * 1.20 = 빠르게
     * 1.40 = 매우 빠르게
     */
    @Column(
            name = "voice_speed",
            nullable = false,
            precision = 3,
            scale = 2
    )
    private BigDecimal voiceSpeed;

    // 기존 접근성 설정 변경

    public void update(
            boolean voiceGuidanceEnabled,
            boolean highContrastEnabled,
            FontSize fontSize,
            BigDecimal voiceSpeed
    ) {
        this.voiceGuidanceEnabled = voiceGuidanceEnabled;
        this.highContrastEnabled = highContrastEnabled;
        this.fontSize = fontSize;
        this.voiceSpeed = voiceSpeed;
    }
}