package lk.it3130.ridelink.driver.dto;

import lk.it3130.ridelink.driver.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private Double latitude;
    private Double longitude;
    private String areaName;
    private Instant updatedAt;

    public static LocationDto fromModel(Location location) {
        if (location == null) {
            return null;
        }
        return LocationDto.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .areaName(location.getAreaName())
                .updatedAt(location.getUpdatedAt())
                .build();
    }
}
