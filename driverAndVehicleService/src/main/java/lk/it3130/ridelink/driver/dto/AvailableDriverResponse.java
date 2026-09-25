package lk.it3130.ridelink.driver.dto;

import lk.it3130.ridelink.driver.model.AvailabilityStatus;
import lk.it3130.ridelink.driver.model.DriverProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableDriverResponse {

    private String driverId;
    private String accountId;
    private String serviceArea;
    private AvailabilityStatus availability;
    private VehicleDto vehicle;
    private LocationDto currentLocation;

    public static AvailableDriverResponse fromModel(DriverProfile profile) {
        if (profile == null) {
            return null;
        }
        return AvailableDriverResponse.builder()
                .driverId(profile.getId())
                .accountId(profile.getAccountId())
                .serviceArea(profile.getServiceArea())
                .availability(profile.getAvailability())
                .vehicle(VehicleDto.fromModel(profile.getVehicle()))
                .currentLocation(LocationDto.fromModel(profile.getCurrentLocation()))
                .build();
    }
}
