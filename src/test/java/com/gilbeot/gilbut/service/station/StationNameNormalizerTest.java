package com.gilbeot.gilbut.service.station;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StationNameNormalizerTest {

    @Test
    void normalizesStationNames() {
        assertThat(
                StationNameNormalizer.normalize("수원역")
        ).isEqualTo("수원");
        assertThat(
                StationNameNormalizer.normalize("수원역 1호선")
        ).isEqualTo("수원");
        assertThat(
                StationNameNormalizer.normalize("광교중앙역(아주대)")
        ).isEqualTo("광교중앙");
        assertThat(
                StationNameNormalizer.normalize("수인분당선 망포역")
        ).isEqualTo("망포");
        assertThat(
                StationNameNormalizer.normalize("수원역[1호선]")
        ).isEqualTo("수원");
        assertThat(
                StationNameNormalizer.normalize("수원시청역[수인분당선]")
        ).isEqualTo("수원시청");
    }
}
