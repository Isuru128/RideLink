package lk.it3130.ridelink.driver.dto;

import jakarta.validation.constraints.NotNull;
import lk.it3130.ridelink.driver.model.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvailabilityRequest {

    @NotNull(message = "Availability status is required (OFFLINE, AVAILABLE, BUSY)")
    private AvailabilityStatus availability;
}
