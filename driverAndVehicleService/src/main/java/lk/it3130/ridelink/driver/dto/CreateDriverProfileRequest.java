package lk.it3130.ridelink.driver.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverProfileRequest {

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @NotBlank(message = "Service area is required")
    private String serviceArea;

    private UpdateVehicleRequest initialVehicle;
}
