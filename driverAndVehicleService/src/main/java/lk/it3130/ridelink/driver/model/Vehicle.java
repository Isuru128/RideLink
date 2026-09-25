package lk.it3130.ridelink.driver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    private String make;
    private String model;
    private String licensePlate;
    private Integer capacity;
    private VehicleType vehicleType;
    private String color;
}
