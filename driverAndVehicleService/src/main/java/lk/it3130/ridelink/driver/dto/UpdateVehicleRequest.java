package lk.it3130.ridelink.driver.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.it3130.ridelink.driver.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleRequest {

    @NotBlank(message = "Vehicle make is required")
    private String make;

    @NotBlank(message = "Vehicle model is required")
    private String model;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Passenger capacity is required")
    @Min(value = 1, message = "Passenger capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Vehicle color is required")
    private String color;
}
