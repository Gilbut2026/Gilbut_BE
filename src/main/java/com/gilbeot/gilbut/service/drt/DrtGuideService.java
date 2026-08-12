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

    private static final String GUIDE_MESSAGE =
            "이 지역은 똑버스 운행 지역이에요. "
                    + "실제 호출 가능한 정류장과 배차 여부는 "
                    + "똑타 앱에서 확인해 주세요.";

    private final TmapReverseGeocodingClient
            tmapReverseGeocodingClient;

    private final DrtAreaResolver drtAreaResolver;

    public DrtGuideResponse createGuide(
            RouteCandidateRequest request,
            AiRouteScoringResponse.DrtDecision drtDecision
    ) {
        if (!shouldCheck(drtDecision)
                || !hasCoordinates(request)) {

            return null;
        }

        Optional<TmapReverseGeocodingResponse.AddressInfo>
                originAddress =
                tmapReverseGeocodingClient.search(
                        request.getOriginLatitude(),
                        request.getOriginLongitude()
                );

        if (originAddress.isEmpty()) {
            return null;
        }

        Optional<DrtServiceArea> originArea =
                drtAreaResolver.resolve(
                        originAddress.get()
                );

        if (originArea.isEmpty()) {
            return null;
        }

        Optional<TmapReverseGeocodingResponse.AddressInfo>
                destinationAddress =
                tmapReverseGeocodingClient.search(
                        request.getDestinationLatitude(),
                        request.getDestinationLongitude()
                );

        if (destinationAddress.isEmpty()) {
            return null;
        }

        Optional<DrtServiceArea> destinationArea =
                drtAreaResolver.resolve(
                        destinationAddress.get()
                );

        if (destinationArea.isEmpty()
                || originArea.get()
                != destinationArea.get()) {

            return null;
        }

        DrtServiceArea serviceArea =
                originArea.get();

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
                        GUIDE_MESSAGE
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