package com.gilbeot.gilbut.service.drt;

import com.gilbeot.gilbut.client.tmap.dto.geocoding.TmapReverseGeocodingResponse;
import com.gilbeot.gilbut.dto.drt.DrtServiceArea;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

@Component
public class DrtAreaResolver {

    private static final Set<String> GOSAEK_ADMIN_DONGS =
            Set.of("평동");

    private static final Set<String> GOSAEK_LEGAL_DONGS =
            Set.of(
                    "고색동",
                    "오목천동",
                    "평동",
                    "평리동"
            );

    private static final Set<String> GWANGGYO_ADMIN_DONGS =
            Set.of(
                    "광교1동",
                    "광교2동"
            );

    private static final Set<String> GWONSEON_LEGAL_DONGS =
            Set.of("권선동");

    private static final Set<String> DANGSU_LEGAL_DONGS =
            Set.of("당수동");

    public Optional<DrtServiceArea> resolve(
            TmapReverseGeocodingResponse.AddressInfo addressInfo
    ) {
        if (addressInfo == null || !isSuwon(addressInfo)) {
            return Optional.empty();
        }

        String adminDong =
                normalize(addressInfo.getAdminDong());

        String legalDong =
                normalize(addressInfo.getLegalDong());

        if (GOSAEK_ADMIN_DONGS.contains(adminDong)
                || GOSAEK_LEGAL_DONGS.contains(legalDong)) {

            return Optional.of(
                    DrtServiceArea.GOSAEK_OMOKCHEON_PYEONGRI
            );
        }

        if (GWANGGYO_ADMIN_DONGS.contains(adminDong)) {
            return Optional.of(
                    DrtServiceArea.GWANGGYO
            );
        }

        if (GWONSEON_LEGAL_DONGS.contains(legalDong)) {
            return Optional.of(
                    DrtServiceArea.GWONSEON
            );
        }

        if (DANGSU_LEGAL_DONGS.contains(legalDong)) {
            return Optional.of(
                    DrtServiceArea.DANGSU
            );
        }

        return Optional.empty();
    }

    private boolean isSuwon(
            TmapReverseGeocodingResponse.AddressInfo addressInfo
    ) {
        String locationText =
                String.join(
                        " ",
                        valueOrEmpty(
                                addressInfo.getCityDo()
                        ),
                        valueOrEmpty(
                                addressInfo.getGuGun()
                        ),
                        valueOrEmpty(
                                addressInfo.getFullAddress()
                        )
                );

        return locationText.contains("수원");
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim()
                .replace(" ", "");
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}