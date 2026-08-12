package com.gilbeot.gilbut.service.drt;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.client.tmap.TmapReverseGeocodingClient;
import com.gilbeot.gilbut.client.tmap.dto.geocoding.TmapReverseGeocodingResponse;
import com.gilbeot.gilbut.dto.drt.DrtAvailability;
import com.gilbeot.gilbut.dto.drt.DrtGuideResponse;
import com.gilbeot.gilbut.dto.drt.DrtServiceArea;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrtGuideServiceTest {

    @Mock
    private TmapReverseGeocodingClient
            tmapReverseGeocodingClient;

    @Mock
    private DrtAreaResolver drtAreaResolver;

    private DrtGuideService drtGuideService;

    @BeforeEach
    void setUp() {
        drtGuideService =
                new DrtGuideService(
                        tmapReverseGeocodingClient,
                        drtAreaResolver
                );
    }

    @Test
    @DisplayName("AI가 DRT를 추천하고 출도착지가 같은 권역이면 똑버스 안내를 생성한다")
    void createsGuideForSameArea() {
        RouteCandidateRequest request =
                request();

        AiRouteScoringResponse.DrtDecision decision =
                decision(
                        true,
                        false
                );

        TmapReverseGeocodingResponse.AddressInfo
                originAddress =
                address(
                        "광교1동",
                        "이의동"
                );

        TmapReverseGeocodingResponse.AddressInfo
                destinationAddress =
                address(
                        "광교2동",
                        "하동"
                );

        when(
                tmapReverseGeocodingClient.search(
                        request.getOriginLatitude(),
                        request.getOriginLongitude()
                )
        ).thenReturn(
                Optional.of(originAddress)
        );

        when(
                drtAreaResolver.resolve(
                        originAddress
                )
        ).thenReturn(
                Optional.of(
                        DrtServiceArea.GWANGGYO
                )
        );

        when(
                tmapReverseGeocodingClient.search(
                        request.getDestinationLatitude(),
                        request.getDestinationLongitude()
                )
        ).thenReturn(
                Optional.of(destinationAddress)
        );

        when(
                drtAreaResolver.resolve(
                        destinationAddress
                )
        ).thenReturn(
                Optional.of(
                        DrtServiceArea.GWANGGYO
                )
        );

        DrtGuideResponse result =
                drtGuideService.createGuide(
                        request,
                        decision
                );

        assertThat(result)
                .isNotNull();

        assertThat(
                result.getShow()
        ).isTrue();

        assertThat(
                result.getServiceName()
        ).isEqualTo(
                "수원 똑버스"
        );

        assertThat(
                result.getServiceArea()
        ).isEqualTo(
                DrtServiceArea.GWANGGYO
        );

        assertThat(
                result.getServiceAreaName()
        ).isEqualTo(
                "광교1·2동"
        );

        assertThat(
                result.getAvailability()
        ).isEqualTo(
                DrtAvailability.CHECK_REQUIRED
        );
    }

    @Test
    @DisplayName("출도착지가 다른 똑버스 권역이면 안내하지 않는다")
    void doesNotCreateGuideForDifferentAreas() {
        RouteCandidateRequest request =
                request();

        AiRouteScoringResponse.DrtDecision decision =
                decision(
                        true,
                        false
                );

        TmapReverseGeocodingResponse.AddressInfo
                originAddress =
                address(
                        "광교1동",
                        "이의동"
                );

        TmapReverseGeocodingResponse.AddressInfo
                destinationAddress =
                address(
                        "입북동",
                        "당수동"
                );

        when(
                tmapReverseGeocodingClient.search(
                        request.getOriginLatitude(),
                        request.getOriginLongitude()
                )
        ).thenReturn(
                Optional.of(originAddress)
        );

        when(
                drtAreaResolver.resolve(
                        originAddress
                )
        ).thenReturn(
                Optional.of(
                        DrtServiceArea.GWANGGYO
                )
        );

        when(
                tmapReverseGeocodingClient.search(
                        request.getDestinationLatitude(),
                        request.getDestinationLongitude()
                )
        ).thenReturn(
                Optional.of(destinationAddress)
        );

        when(
                drtAreaResolver.resolve(
                        destinationAddress
                )
        ).thenReturn(
                Optional.of(
                        DrtServiceArea.DANGSU
                )
        );

        assertThat(
                drtGuideService.createGuide(
                        request,
                        decision
                )
        ).isNull();
    }

    @Test
    @DisplayName("AI가 DRT를 추천하지 않으면 리버스 지오코딩을 호출하지 않는다")
    void skipsWhenDrtIsNotRecommended() {
        RouteCandidateRequest request =
                request();

        AiRouteScoringResponse.DrtDecision decision =
                decision(
                        false,
                        false
                );

        assertThat(
                drtGuideService.createGuide(
                        request,
                        decision
                )
        ).isNull();

        verify(
                tmapReverseGeocodingClient,
                never()
        ).search(
                request.getOriginLatitude(),
                request.getOriginLongitude()
        );
    }

    @Test
    @DisplayName("콜택시 안내 대상이면 똑버스 권역을 조회하지 않는다")
    void skipsForTaxiGuide() {
        RouteCandidateRequest request =
                request();

        AiRouteScoringResponse.DrtDecision decision =
                decision(
                        true,
                        true
                );

        assertThat(
                drtGuideService.createGuide(
                        request,
                        decision
                )
        ).isNull();

        verify(
                tmapReverseGeocodingClient,
                never()
        ).search(
                request.getOriginLatitude(),
                request.getOriginLongitude()
        );
    }

    private RouteCandidateRequest request() {
        return RouteCandidateRequest.builder()
                .originLatitude(37.29)
                .originLongitude(127.05)
                .destinationLatitude(37.30)
                .destinationLongitude(127.06)
                .build();
    }

    private AiRouteScoringResponse.DrtDecision decision(
            boolean show,
            boolean taxiGuide
    ) {
        return AiRouteScoringResponse
                .DrtDecision
                .builder()
                .show(show)
                .priority(false)
                .taxiGuide(taxiGuide)
                .build();
    }

    private TmapReverseGeocodingResponse.AddressInfo address(
            String adminDong,
            String legalDong
    ) {
        return TmapReverseGeocodingResponse
                .AddressInfo
                .builder()
                .fullAddress(
                        "경기도 수원시"
                )
                .cityDo("경기도")
                .guGun("수원시")
                .adminDong(adminDong)
                .legalDong(legalDong)
                .build();
    }
}