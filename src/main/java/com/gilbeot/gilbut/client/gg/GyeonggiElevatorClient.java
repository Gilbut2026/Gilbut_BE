package com.gilbeot.gilbut.client.gg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbeot.gilbut.domain.station.StationElevator;
import com.gilbeot.gilbut.global.common.code.ErrorCode;
import com.gilbeot.gilbut.global.exception.CustomException;
import com.gilbeot.gilbut.service.station.StationNameNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GyeonggiElevatorClient {

    private static final String API_URL =
            "https://openapi.gg.go.kr/TBGGSTATNELVM";
    private static final String SERVICE_NAME =
            "TBGGSTATNELVM";
    private static final int PAGE_SIZE = 1000;
    private static final Duration CACHE_TTL =
            Duration.ofHours(12);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gyeonggi.elevator.api-key:}")
    private String apiKey;

    private List<StationElevator> cachedElevators = List.of();
    private Instant cachedAt = Instant.EPOCH;

    public synchronized List<StationElevator> getElevators() {
        if (isCacheValid()) {
            return cachedElevators;
        }

        try {
            List<StationElevator> elevators =
                    fetchAllElevators();
            cachedElevators = List.copyOf(elevators);
            cachedAt = Instant.now();

            return cachedElevators;

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            log.error("경기도 역사 내 승강기 조회 처리 중 오류 발생", e);
            throw new CustomException(
                    ErrorCode.ELEVATOR_SEARCH_FAILED
            );
        }
    }

    private boolean isCacheValid() {
        return !cachedElevators.isEmpty()
                && cachedAt.plus(CACHE_TTL)
                .isAfter(Instant.now());
    }

    private List<StationElevator> fetchAllElevators()
            throws Exception {
        if (!StringUtils.hasText(apiKey)) {
            throw new CustomException(
                    ErrorCode.ELEVATOR_SEARCH_FAILED
            );
        }

        List<StationElevator> allElevators =
                new ArrayList<>();
        int page = 1;

        while (true) {
            ElevatorPage elevatorPage = fetchPage(page);
            allElevators.addAll(elevatorPage.elevators());

            if (elevatorPage.elevators().isEmpty()) {
                break;
            }

            if (elevatorPage.totalCount() > 0
                    && allElevators.size()
                    >= elevatorPage.totalCount()) {
                break;
            }

            page++;
        }

        return allElevators;
    }

    private ElevatorPage fetchPage(
            int page
    ) throws Exception {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(API_URL)
                .queryParam("KEY", apiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", page)
                .queryParam("pSize", PAGE_SIZE)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();
        String body =
                restTemplate.getForObject(uri, String.class);
        JsonNode payload =
                objectMapper.readTree(body);

        return parseResponse(payload);
    }

    private ElevatorPage parseResponse(
            JsonNode payload
    ) {
        JsonNode serviceBlocks = payload.get(SERVICE_NAME);

        if (serviceBlocks == null || !serviceBlocks.isArray()) {
            throw new CustomException(
                    ErrorCode.ELEVATOR_SEARCH_FAILED
            );
        }

        List<StationElevator> elevators =
                new ArrayList<>();
        int totalCount = 0;

        for (JsonNode block : serviceBlocks) {
            totalCount =
                    parseHead(
                            block.get("head"),
                            totalCount
                    );

            JsonNode rows = block.get("row");

            if (rows != null && rows.isArray()) {
                rows.forEach(row ->
                        toElevator(row)
                                .stream()
                                .forEach(elevators::add)
                );
            }
        }

        return new ElevatorPage(
                elevators,
                totalCount
        );
    }

    private int parseHead(
            JsonNode head,
            int currentTotalCount
    ) {
        int totalCount = currentTotalCount;

        if (head == null || !head.isArray()) {
            return totalCount;
        }

        for (JsonNode headItem : head) {
            if (headItem.has("list_total_count")) {
                totalCount =
                        safeInt(
                                headItem.get("list_total_count")
                                        .asText(),
                                totalCount
                        );
            }

            JsonNode result = headItem.get("RESULT");

            if (result != null) {
                String code = textOrNull(result, "CODE");

                if (StringUtils.hasText(code)
                        && !"INFO-000".equals(code)) {
                    throw new CustomException(
                            ErrorCode.ELEVATOR_SEARCH_FAILED
                    );
                }
            }
        }

        return totalCount;
    }

    private List<StationElevator> toElevator(
            JsonNode row
    ) {
        String stationName = textOrNull(row, "STATN_NM");
        String normalizedStationName =
                StationNameNormalizer.normalize(stationName);

        if (!StringUtils.hasText(normalizedStationName)) {
            return List.of();
        }

        return List.of(
                StationElevator.builder()
                        .stationName(stationName)
                        .normalizedStationName(
                                normalizedStationName
                        )
                        .operator(
                                textOrNull(
                                        row,
                                        "RAILROAD_OPR_INST_NM"
                                )
                        )
                        .routeName(textOrNull(row, "OPR_ROUTE_NM"))
                        .exitNumber(textOrNull(row, "ETRA_NO"))
                        .location(textOrNull(row, "LOC"))
                        .floorRange(formatFloorRange(row))
                        .state(textOrNull(row, "ELV_STATE"))
                        .elevatorNumber(textOrNull(row, "ELV_NO"))
                        .capacityCount(textOrNull(row, "FNOP_CNT"))
                        .capacityWeight(textOrNull(row, "FNOP_WT"))
                        .build()
        );
    }

    private String formatFloorRange(
            JsonNode row
    ) {
        String startFloor = textOrNull(row, "BGNG_FLOOR_NM");
        String endFloor = textOrNull(row, "END_FLOOR_NM");
        String startCount =
                textOrNull(row, "BGNG_OPRT_FLOOR_CNT");
        String endCount =
                textOrNull(row, "END_OPRT_FLOOR_CNT");

        String startText =
                StringUtils.hasText(startFloor)
                        ? startFloor
                        : StringUtils.hasText(startCount)
                        ? startCount
                        : "-";
        String endText =
                StringUtils.hasText(endFloor)
                        ? endFloor
                        : StringUtils.hasText(endCount)
                        ? endCount
                        : "-";

        return startText + " -> " + endText;
    }

    private String textOrNull(
            JsonNode node,
            String fieldName
    ) {
        if (node == null || node.get(fieldName) == null) {
            return null;
        }

        String value = node.get(fieldName).asText();

        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private int safeInt(
            String value,
            int defaultValue
    ) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }

        try {
            return (int) Double.parseDouble(value.trim());

        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private record ElevatorPage(
            List<StationElevator> elevators,
            int totalCount
    ) {
    }
}
