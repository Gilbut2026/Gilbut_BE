package com.gilbeot.gilbut.service.station;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class StationNameNormalizer {

    private static final Pattern PARENTHESIS_PATTERN =
            Pattern.compile("\\([^)]*\\)");
    private static final Pattern BRACKET_PATTERN =
            Pattern.compile("\\[[^]]*]");
    private static final Pattern LINE_PATTERN =
            Pattern.compile(
                    "(수도권\\s*)?\\d+\\s*호선"
                            + "|수인\\s*분당선"
                            + "|수인분당선"
                            + "|신분당선"
                            + "|분당선"
                            + "|경부선"
                            + "|경인선"
                            + "|장항선"
                            + "|일반호선"
                            + "|GTX[-\\s]?[A-Z]"
                            + "|KTX"
                            + "|SRT",
                    Pattern.CASE_INSENSITIVE
            );
    private static final Pattern SPACE_PATTERN =
            Pattern.compile("\\s+");

    private StationNameNormalizer() {
    }

    public static String normalize(
            String name
    ) {
        if (!StringUtils.hasText(name)) {
            return "";
        }

        String stationName = name.trim();
        stationName =
                PARENTHESIS_PATTERN.matcher(stationName)
                        .replaceAll("");
        stationName =
                BRACKET_PATTERN.matcher(stationName)
                        .replaceAll("");
        stationName =
                LINE_PATTERN.matcher(stationName)
                        .replaceAll("");
        stationName =
                SPACE_PATTERN.matcher(stationName)
                        .replaceAll("");

        if (stationName.endsWith("역")) {
            stationName =
                    stationName.substring(
                            0,
                            stationName.length() - 1
                    );
        }

        return stationName;
    }
}
