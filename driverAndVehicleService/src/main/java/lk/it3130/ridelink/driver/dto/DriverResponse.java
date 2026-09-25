package lk.it3130.ridelink.driver.dto;

import lk.it3130.ridelink.driver.model.AvailabilityStatus;
import lk.it3130.ridelink.driver.model.DriverAccountStatus;
import lk.it3130.ridelink.driver.model.DriverProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private String id;
    private String accountId;
    private String licenseNumber;
    private Integer experienceYears;
    private String serviceArea;
    private AvailabilityStatus availability;
    private DriverAccountStatus status;
    private VehicleDto vehicle;
    private LocationDto currentLocation;
    private Instant createdAt;
    private Instant updatedAt;

    public static DriverResponse fromModel(DriverProfile profile) {
        if (profile == null) {
            return null;
        }
        return DriverResponse.builder()
                .id(profile.getId())
                .accountId(profile.getAccountId())
                .licenseNumber(profile.getLicenseNumber())
                .experienceYears(profile.getExperienceYears())
                .serviceArea(profile.getServiceArea())
                .availability(profile.getAvailability())
                .status(profile.getStatus())
                .vehicle(VehicleDto.fromModel(profile.getVehicle()))
                .currentLocation(LocationDto.fromModel(profile.getCurrentLocation()))
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
