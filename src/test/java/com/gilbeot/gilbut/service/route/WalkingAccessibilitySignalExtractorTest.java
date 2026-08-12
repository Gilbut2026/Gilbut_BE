package com.gilbeot.gilbut.service.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gilbeot.gilbut.client.tmap.dto.walking.TmapWalkingRouteResponse;
import com.gilbeot.gilbut.dto.route.RouteAccessibilitySignals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WalkingAccessibilitySignalExtractorTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private final WalkingAccessibilitySignalExtractor extractor =
            new WalkingAccessibilitySignalExtractor();

    @Test
    @DisplayName("facilityType을 기준으로 계단 육교 지하보도를 추출한다")
    void extractAccessibilitySignalsFromFacilityType()
            throws Exception {

        TmapWalkingRouteResponse response =
                objectMapper.readValue(
                        """
                        {
                          "features": [
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.0, 37.0],
                                  [127.1, 37.1]
                                ]
                              },
                              "properties": {
                                "facilityType": 17
                              }
                            },
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.1, 37.1],
                                  [127.2, 37.2]
                                ]
                              },
                              "properties": {
                                "facilityType": 12
                              }
                            },
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.2, 37.2],
                                  [127.3, 37.3]
                                ]
                              },
                              "properties": {
                                "facilityType": 14
                              }
                            }
                          ]
                        }
                        """,
                        TmapWalkingRouteResponse.class
                );

        RouteAccessibilitySignals result =
                extractor.extract(response);

        assertThat(result.getStair().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.PRESENT
                );

        assertThat(result.getStair().getCount())
                .isEqualTo(1);

        assertThat(result.getOverpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.PRESENT
                );

        assertThat(result.getOverpass().getCount())
                .isEqualTo(1);

        assertThat(result.getUnderpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.PRESENT
                );

        assertThat(result.getUnderpass().getCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("facilityType이 있으면 동일 시설의 turnType을 중복 집계하지 않는다")
    void preferFacilityTypeOverTurnType()
            throws Exception {

        TmapWalkingRouteResponse response =
                objectMapper.readValue(
                        """
                        {
                          "features": [
                            {
                              "geometry": {
                                "type": "Point",
                                "coordinates": [127.0, 37.0]
                              },
                              "properties": {
                                "turnType": 125
                              }
                            },
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.0, 37.0],
                                  [127.1, 37.1]
                                ]
                              },
                              "properties": {
                                "facilityType": 12
                              }
                            }
                          ]
                        }
                        """,
                        TmapWalkingRouteResponse.class
                );

        RouteAccessibilitySignals result =
                extractor.extract(response);

        assertThat(result.getOverpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.PRESENT
                );

        assertThat(result.getOverpass().getCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("facilityType이 없으면 turnType을 보조 근거로 사용한다")
    void fallbackToTurnType()
            throws Exception {

        TmapWalkingRouteResponse response =
                objectMapper.readValue(
                        """
                        {
                          "features": [
                            {
                              "geometry": {
                                "type": "Point",
                                "coordinates": [127.0, 37.0]
                              },
                              "properties": {
                                "turnType": 125
                              }
                            },
                            {
                              "geometry": {
                                "type": "Point",
                                "coordinates": [127.1, 37.1]
                              },
                              "properties": {
                                "turnType": 126
                              }
                            },
                            {
                              "geometry": {
                                "type": "Point",
                                "coordinates": [127.2, 37.2]
                              },
                              "properties": {
                                "turnType": 127
                              }
                            },
                            {
                              "geometry": {
                                "type": "Point",
                                "coordinates": [127.3, 37.3]
                              },
                              "properties": {
                                "turnType": 129
                              }
                            }
                          ]
                        }
                        """,
                        TmapWalkingRouteResponse.class
                );

        RouteAccessibilitySignals result =
                extractor.extract(response);

        assertThat(result.getOverpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.PRESENT
                );

        assertThat(result.getOverpass().getCount())
                .isEqualTo(1);

        assertThat(result.getUnderpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.PRESENT
                );

        assertThat(result.getUnderpass().getCount())
                .isEqualTo(1);

        assertThat(result.getStair().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.PRESENT
                );

        assertThat(result.getStair().getCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("연속된 동일 facilityType은 하나의 시설 구간으로 집계한다")
    void doNotDuplicateContinuousFacilitySegments()
            throws Exception {

        TmapWalkingRouteResponse response =
                objectMapper.readValue(
                        """
                        {
                          "features": [
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.0, 37.0],
                                  [127.1, 37.1]
                                ]
                              },
                              "properties": {
                                "facilityType": 17
                              }
                            },
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.1, 37.1],
                                  [127.2, 37.2]
                                ]
                              },
                              "properties": {
                                "facilityType": 17
                              }
                            },
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.2, 37.2],
                                  [127.3, 37.3]
                                ]
                              },
                              "properties": {
                                "facilityType": 0
                              }
                            },
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.3, 37.3],
                                  [127.4, 37.4]
                                ]
                              },
                              "properties": {
                                "facilityType": 17
                              }
                            }
                          ]
                        }
                        """,
                        TmapWalkingRouteResponse.class
                );

        RouteAccessibilitySignals result =
                extractor.extract(response);

        assertThat(result.getStair().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.PRESENT
                );

        assertThat(result.getStair().getCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("정상 응답에 접근성 시설이 없으면 ABSENT로 반환한다")
    void returnAbsentWhenNoObstacleExists()
            throws Exception {

        TmapWalkingRouteResponse response =
                objectMapper.readValue(
                        """
                        {
                          "features": [
                            {
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                  [127.0, 37.0],
                                  [127.1, 37.1]
                                ]
                              },
                              "properties": {
                                "facilityType": 0
                              }
                            }
                          ]
                        }
                        """,
                        TmapWalkingRouteResponse.class
                );

        RouteAccessibilitySignals result =
                extractor.extract(response);

        assertThat(result.getStair().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.ABSENT
                );

        assertThat(result.getStair().getCount())
                .isZero();

        assertThat(result.getOverpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.ABSENT
                );

        assertThat(result.getUnderpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.ABSENT
                );
    }

    @Test
    @DisplayName("응답 자체를 확인할 수 없으면 UNKNOWN으로 반환한다")
    void returnUnknownWhenResponseIsUnavailable() {

        RouteAccessibilitySignals result =
                extractor.extract(null);

        assertThat(result.getStair().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.UNKNOWN
                );

        assertThat(result.getStair().getCount())
                .isNull();

        assertThat(result.getOverpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.UNKNOWN
                );

        assertThat(result.getUnderpass().getState())
                .isEqualTo(
                        RouteAccessibilitySignals.State.UNKNOWN
                );
    }
}