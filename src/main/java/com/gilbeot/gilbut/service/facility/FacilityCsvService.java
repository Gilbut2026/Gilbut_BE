package com.gilbeot.gilbut.service.facility;

import com.gilbeot.gilbut.domain.facility.Facility;
import com.gilbeot.gilbut.domain.facility.FacilityType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FacilityCsvService {

    private static final String SHELTER_CSV_PATH =
            "data/facilities/수원시_쉼터_통합.csv";
    private static final String TOILET_CSV_PATH =
            "data/facilities/수원시_화장실_정리.csv";

    private List<Facility> facilities = List.of();

    @PostConstruct
    public void loadFacilities() {
        List<Facility> loadedFacilities = new ArrayList<>();

        loadedFacilities.addAll(
                loadCsv(SHELTER_CSV_PATH, FacilityType.SHELTER)
        );
        loadedFacilities.addAll(
                loadCsv(TOILET_CSV_PATH, FacilityType.TOILET)
        );

        facilities = List.copyOf(loadedFacilities);

        log.info(
                "Loaded {} nearby facilities from CSV resources",
                facilities.size()
        );
    }

    public List<Facility> findByTypes(
            Set<FacilityType> types
    ) {
        Set<FacilityType> requestedTypes =
                types == null || types.isEmpty()
                        ? EnumSet.allOf(FacilityType.class)
                        : EnumSet.copyOf(types);

        return facilities.stream()
                .filter(facility ->
                        requestedTypes.contains(facility.getType())
                )
                .toList();
    }

    private List<Facility> loadCsv(
            String path,
            FacilityType type
    ) {
        ClassPathResource resource = new ClassPathResource(path);

        try (
                Reader reader = new BufferedReader(
                        new InputStreamReader(
                                resource.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
                CSVParser parser =
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setTrim(true)
                                .build()
                                .parse(reader)
        ) {
            List<Facility> result = new ArrayList<>();

            for (CSVRecord record : parser) {
                result.add(toFacility(record, type));
            }

            return result;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "시설 CSV 파일을 읽을 수 없습니다: " + path,
                    e
            );
        }
    }

    private Facility toFacility(
            CSVRecord record,
            FacilityType type
    ) {
        return Facility.builder()
                .type(type)
                .sourceId(textOrNull(record, "id"))
                .name(textOrNull(record, "name"))
                .category(textOrNull(record, "category"))
                .subcategory(textOrNull(record, "subcategory"))
                .address(textOrNull(record, "address"))
                .latitude(parseCoordinate(record, "lat"))
                .longitude(parseCoordinate(record, "lng"))
                .phone(textOrNull(record, "phone"))
                .operatingHours(textOrNull(record, "operating_hours"))
                .status(textOrNull(record, "status"))
                .sourceDate(textOrNull(record, "source_date"))
                .build();
    }

    private String textOrNull(
            CSVRecord record,
            String name
    ) {
        if (!record.isMapped(name)) {
            return null;
        }

        String value = record.get(name);

        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private double parseCoordinate(
            CSVRecord record,
            String name
    ) {
        String value = textOrNull(record, name);

        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(
                    "시설 CSV 좌표 값이 비어 있습니다. row="
                            + record.getRecordNumber()
                            + ", column="
                            + name
            );
        }

        try {
            return Double.parseDouble(value);

        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "시설 CSV 좌표 형식이 올바르지 않습니다. row="
                            + record.getRecordNumber()
                            + ", column="
                            + name
                            + ", value="
                            + value,
                    e
            );
        }
    }
}
