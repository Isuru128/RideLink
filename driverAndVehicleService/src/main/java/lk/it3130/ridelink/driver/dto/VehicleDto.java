package lk.it3130.ridelink.driver.dto;

import lk.it3130.ridelink.driver.model.Vehicle;
import lk.it3130.ridelink.driver.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

    private String make;
    private String model;
    private String licensePlate;
    private Integer capacity;
    private VehicleType vehicleType;
    private String color;

    public static VehicleDto fromModel(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return VehicleDto.builder()
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .licensePlate(vehicle.getLicensePlate())
                .capacity(vehicle.getCapacity())
                .vehicleType(vehicle.getVehicleType())
                .color(vehicle.getColor())
                .build();
    }
}
