package com.gilbeot.gilbut.service.drt;

import com.gilbeot.gilbut.client.ai.dto.scoring.AiRouteScoringResponse;
import com.gilbeot.gilbut.client.tmap.TmapReverseGeocodingClient;
import com.gilbeot.gilbut.client.tmap.dto.geocoding.TmapReverseGeocodingResponse;
import com.gilbeot.gilbut.dto.drt.DrtAvailability;
import com.gilbeot.gilbut.dto.drt.DrtGuideResponse;
import com.gilbeot.gilbut.dto.drt.DrtServiceArea;
import com.gilbeot.gilbut.dto.route.RouteCandidateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrtGuideService {

    private static final String SERVICE_NAME =
            "수원 똑버스";

    private static final String AVAILABLE_MESSAGE =
            "이 지역은 똑버스 운행 지역이에요. "
                    + "실제 호출 가능한 정류장과 배차 여부는 "
                    + "똑타 앱에서 확인해 주세요.";

    private static final String OUT_OF_SERVICE_AREA_MESSAGE =
            "출발지와 도착지가 같은 똑버스 운행 권역에 포함되지 않아요.";

    private static final String UNKNOWN_MESSAGE =
            "똑버스 운행 가능 여부를 확인하지 못했어요.";

    private final TmapReverseGeocodingClient
            tmapReverseGeocodingClient;

    private final DrtAreaResolver drtAreaResolver;

    public DrtGuideResponse createGuide(
            RouteCandidateRequest request,
            AiRouteScoringResponse.DrtDecision drtDecision
    ) {
        if (!shouldCheck(drtDecision)) {
            return null;
        }

        if (!hasCoordinates(request)) {
            return unknownGuide();
        }

        Optional<TmapReverseGeocodingResponse.AddressInfo>
                originAddress =
                tmapReverseGeocodingClient.search(
                        request.getOriginLatitude(),
                        request.getOriginLongitude()
                );

        if (originAddress.isEmpty()) {
            return unknownGuide();
        }

        Optional<DrtServiceArea> originArea =
                drtAreaResolver.resolve(
                        originAddress.get()
                );

        if (originArea.isEmpty()) {
            return outOfServiceAreaGuide();
        }

        Optional<TmapReverseGeocodingResponse.AddressInfo>
                destinationAddress =
                tmapReverseGeocodingClient.search(
                        request.getDestinationLatitude(),
                        request.getDestinationLongitude()
                );

        if (destinationAddress.isEmpty()) {
            return unknownGuide();
        }

        Optional<DrtServiceArea> destinationArea =
                drtAreaResolver.resolve(
                        destinationAddress.get()
                );

        if (destinationArea.isEmpty()) {
            return outOfServiceAreaGuide();
        }

        if (originArea.get()
                != destinationArea.get()) {

            return outOfServiceAreaGuide();
        }

        return availableGuide(
                originArea.get()
        );
    }

    private DrtGuideResponse availableGuide(
            DrtServiceArea serviceArea
    ) {
        return DrtGuideResponse.builder()
                .show(true)
                .serviceName(
                        SERVICE_NAME
                )
                .serviceArea(
                        serviceArea
                )
                .serviceAreaName(
                        serviceArea.getDisplayName()
                )
                .availability(
                        DrtAvailability.CHECK_REQUIRED
                )
                .message(
                        AVAILABLE_MESSAGE
                )
                .build();
    }

    private DrtGuideResponse outOfServiceAreaGuide() {
        return DrtGuideResponse.builder()
                .show(false)
                .serviceName(
                        SERVICE_NAME
                )
                .serviceArea(null)
                .serviceAreaName(null)
                .availability(
                        DrtAvailability.OUT_OF_SERVICE_AREA
                )
                .message(
                        OUT_OF_SERVICE_AREA_MESSAGE
                )
                .build();
    }

    private DrtGuideResponse unknownGuide() {
        return DrtGuideResponse.builder()
                .show(false)
                .serviceName(
                        SERVICE_NAME
                )
                .serviceArea(null)
                .serviceAreaName(null)
                .availability(
                        DrtAvailability.UNKNOWN
                )
                .message(
                        UNKNOWN_MESSAGE
                )
                .build();
    }

    private boolean shouldCheck(
            AiRouteScoringResponse.DrtDecision drtDecision
    ) {
        return drtDecision != null
                && Boolean.TRUE.equals(
                drtDecision.getShow()
        )
                && !Boolean.TRUE.equals(
                drtDecision.getTaxiGuide()
        );
    }

    private boolean hasCoordinates(
            RouteCandidateRequest request
    ) {
        return request != null
                && request.getOriginLatitude() != null
                && request.getOriginLongitude() != null
                && request.getDestinationLatitude() != null
                && request.getDestinationLongitude() != null;
    }
}