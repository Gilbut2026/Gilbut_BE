package com.gilbeot.gilbut.domain.user;

import com.gilbeot.gilbut.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "favorite_places")
public class FavoritePlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 즐겨찾기에 표시할 장소명
    @Column(nullable = false, length = 100)
    private String name;

    // TMAP 장소 검색 결과에서 전달받은 주소
    @Column(nullable = false, length = 255)
    private String address;

    // TMAP 장소 검색 결과에서 전달받은 위도
    @Column(nullable = false)
    private Double latitude;

    // TMAP 장소 검색 결과에서 전달받은 경도
    @Column(nullable = false)
    private Double longitude;

    // 즐겨찾기 표시 이름 수정
    public void updateName(String name) {
        this.name = name.trim();
    }
}