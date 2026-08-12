package com.gilbeot.gilbut.service.drt;

import com.gilbeot.gilbut.client.tmap.dto.geocoding.TmapReverseGeocodingResponse;
import com.gilbeot.gilbut.dto.drt.DrtServiceArea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DrtAreaResolverTest {

    private final DrtAreaResolver drtAreaResolver =
            new DrtAreaResolver();

    @Test
    @DisplayName("광교1동은 광교 똑버스 권역으로 판정한다")
    void resolvesGwanggyo() {
        TmapReverseGeocodingResponse.AddressInfo addressInfo =
                address(
                        "경기도 수원시 영통구 이의동",
                        "광교1동",
                        "이의동"
                );

        assertThat(
                drtAreaResolver.resolve(addressInfo)
        ).contains(
                DrtServiceArea.GWANGGYO
        );
    }

    @Test
    @DisplayName("고색동은 고색 오목천 평리 권역으로 판정한다")
    void resolvesGosaek() {
        TmapReverseGeocodingResponse.AddressInfo addressInfo =
                address(
                        "경기도 수원시 권선구 고색동",
                        "평동",
                        "고색동"
                );

        assertThat(
                drtAreaResolver.resolve(addressInfo)
        ).contains(
                DrtServiceArea
                        .GOSAEK_OMOKCHEON_PYEONGRI
        );
    }

    @Test
    @DisplayName("권선동은 권선 똑버스 권역으로 판정한다")
    void resolvesGwonseon() {
        TmapReverseGeocodingResponse.AddressInfo addressInfo =
                address(
                        "경기도 수원시 권선구 권선동",
                        "권선2동",
                        "권선동"
                );

        assertThat(
                drtAreaResolver.resolve(addressInfo)
        ).contains(
                DrtServiceArea.GWONSEON
        );
    }

    @Test
    @DisplayName("당수동은 당수 똑버스 권역으로 판정한다")
    void resolvesDangsu() {
        TmapReverseGeocodingResponse.AddressInfo addressInfo =
                address(
                        "경기도 수원시 권선구 당수동",
                        "입북동",
                        "당수동"
                );

        assertThat(
                drtAreaResolver.resolve(addressInfo)
        ).contains(
                DrtServiceArea.DANGSU
        );
    }

    @Test
    @DisplayName("수원 외 지역은 똑버스 권역으로 판정하지 않는다")
    void rejectsOutsideSuwon() {
        TmapReverseGeocodingResponse.AddressInfo addressInfo =
                TmapReverseGeocodingResponse
                        .AddressInfo
                        .builder()
                        .fullAddress(
                                "경기도 용인시 수지구 상현동"
                        )
                        .cityDo("경기도")
                        .guGun("용인시 수지구")
                        .adminDong("상현1동")
                        .legalDong("상현동")
                        .build();

        assertThat(
                drtAreaResolver.resolve(addressInfo)
        ).isEmpty();
    }

    private TmapReverseGeocodingResponse.AddressInfo address(
            String fullAddress,
            String adminDong,
            String legalDong
    ) {
        return TmapReverseGeocodingResponse
                .AddressInfo
                .builder()
                .fullAddress(fullAddress)
                .cityDo("경기도")
                .guGun("수원시")
                .adminDong(adminDong)
                .legalDong(legalDong)
                .build();
    }
}