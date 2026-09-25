package lk.it3130.ridelink.driver.service;

import lk.it3130.ridelink.driver.dto.AvailableDriverResponse;
import lk.it3130.ridelink.driver.dto.CreateDriverProfileRequest;
import lk.it3130.ridelink.driver.dto.DriverResponse;
import lk.it3130.ridelink.driver.dto.UpdateAvailabilityRequest;
import lk.it3130.ridelink.driver.dto.UpdateLocationRequest;
import lk.it3130.ridelink.driver.dto.UpdateVehicleRequest;
import lk.it3130.ridelink.driver.exception.DriverAlreadyExistsException;
import lk.it3130.ridelink.driver.exception.DriverNotEligibleException;
import lk.it3130.ridelink.driver.exception.DriverNotFoundException;
import lk.it3130.ridelink.driver.exception.UnauthorizedActionException;
import lk.it3130.ridelink.driver.model.AvailabilityStatus;
import lk.it3130.ridelink.driver.model.DriverAccountStatus;
import lk.it3130.ridelink.driver.model.DriverProfile;
import lk.it3130.ridelink.driver.model.Location;
import lk.it3130.ridelink.driver.model.Vehicle;
import lk.it3130.ridelink.driver.model.VehicleType;
import lk.it3130.ridelink.driver.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverProfileRepository driverProfileRepository;

    public DriverResponse createProfile(String accountId,
                                        CreateDriverProfileRequest request,
                                        String currentUserId,
                                        boolean isAdmin) {
        log.info("Creating driver operational profile for accountId: {}", accountId);

        if (!isAdmin && currentUserId != null && !currentUserId.equals(accountId)) {
            throw new UnauthorizedActionException("You are not authorized to create a driver profile for another account");
        }

        if (driverProfileRepository.existsByAccountId(accountId)) {
            throw new DriverAlreadyExistsException("Driver operational profile already exists for account ID: " + accountId);
        }

        Vehicle initialVehicle = null;
        if (request.getInitialVehicle() != null) {
            UpdateVehicleRequest vReq = request.getInitialVehicle();
            initialVehicle = Vehicle.builder()
                    .make(vReq.getMake())
                    .model(vReq.getModel())
                    .licensePlate(vReq.getLicensePlate())
                    .capacity(vReq.getCapacity())
                    .vehicleType(vReq.getVehicleType())
                    .color(vReq.getColor())
                    .build();
        }

        DriverProfile profile = DriverProfile.builder()
                .accountId(accountId)
                .licenseNumber(request.getLicenseNumber())
                .experienceYears(request.getExperienceYears())
                .serviceArea(request.getServiceArea())
                .availability(AvailabilityStatus.OFFLINE)
                .status(DriverAccountStatus.ACTIVE)
                .vehicle(initialVehicle)
                .build();

        DriverProfile saved = driverProfileRepository.save(profile);
        log.info("Driver profile created successfully with ID: {}", saved.getId());
        return DriverResponse.fromModel(saved);
    }

    public DriverResponse getDriverById(String driverId) {
        log.info("Fetching driver profile with ID: {}", driverId);
        DriverProfile profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found with ID: " + driverId));
        return DriverResponse.fromModel(profile);
    }

    public DriverResponse getDriverByAccountId(String accountId) {
        log.info("Fetching driver profile for accountId: {}", accountId);
        DriverProfile profile = driverProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found for account ID: " + accountId));
        return DriverResponse.fromModel(profile);
    }

    public DriverResponse updateVehicle(String driverId,
                                        UpdateVehicleRequest request,
                                        String currentUserId,
                                        boolean isAdmin) {
        log.info("Updating vehicle details for driverId: {}", driverId);
        DriverProfile profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found with ID: " + driverId));

        validateOwnership(profile, currentUserId, isAdmin);

        Vehicle vehicle = Vehicle.builder()
                .make(request.getMake())
                .model(request.getModel())
                .licensePlate(request.getLicensePlate())
                .capacity(request.getCapacity())
                .vehicleType(request.getVehicleType())
                .color(request.getColor())
                .build();

        profile.setVehicle(vehicle);
        DriverProfile updated = driverProfileRepository.save(profile);
        log.info("Vehicle updated successfully for driverId: {}", driverId);
        return DriverResponse.fromModel(updated);
    }

    public DriverResponse updateAvailability(String driverId,
                                             UpdateAvailabilityRequest request,
                                             String currentUserId,
                                             boolean isAdmin) {
        log.info("Updating availability for driverId: {} to {}", driverId, request.getAvailability());
        DriverProfile profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found with ID: " + driverId));

        validateOwnership(profile, currentUserId, isAdmin);

        if (profile.getStatus() == DriverAccountStatus.SUSPENDED) {
            throw new DriverNotEligibleException("Cannot update availability for suspended driver: " + driverId);
        }

        if (request.getAvailability() == AvailabilityStatus.AVAILABLE) {
            if (profile.getVehicle() == null || profile.getVehicle().getLicensePlate() == null) {
                throw new DriverNotEligibleException("Cannot set driver availability to AVAILABLE without an assigned vehicle");
            }
        }

        profile.setAvailability(request.getAvailability());
        DriverProfile updated = driverProfileRepository.save(profile);
        log.info("Availability updated successfully for driverId: {}", driverId);
        return DriverResponse.fromModel(updated);
    }

    public DriverResponse updateLocation(String driverId,
                                         UpdateLocationRequest request,
                                         String currentUserId,
                                         boolean isAdmin) {
        log.info("Updating simulated location for driverId: {}", driverId);
        DriverProfile profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found with ID: " + driverId));

        validateOwnership(profile, currentUserId, isAdmin);

        Location location = Location.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .areaName(request.getAreaName())
                .updatedAt(Instant.now())
                .build();

        profile.setCurrentLocation(location);
        DriverProfile updated = driverProfileRepository.save(profile);
        log.info("Location updated successfully for driverId: {}", driverId);
        return DriverResponse.fromModel(updated);
    }

    public List<AvailableDriverResponse> getEligibleAvailableDrivers(String area,
                                                                   Integer minCapacity,
                                                                   VehicleType vehicleType) {
        log.info("Finding eligible available drivers for area: {}, minCapacity: {}, vehicleType: {}",
                area, minCapacity, vehicleType);

        List<DriverProfile> availableDrivers = driverProfileRepository
                .findByAvailabilityAndStatus(AvailabilityStatus.AVAILABLE, DriverAccountStatus.ACTIVE);

        return availableDrivers.stream()
                .filter(driver -> driver.getVehicle() != null)
                .filter(driver -> matchesArea(driver, area))
                .filter(driver -> minCapacity == null ||
                        (driver.getVehicle().getCapacity() != null && driver.getVehicle().getCapacity() >= minCapacity))
                .filter(driver -> vehicleType == null ||
                        (driver.getVehicle().getVehicleType() != null && driver.getVehicle().getVehicleType() == vehicleType))
                .map(AvailableDriverResponse::fromModel)
                .collect(Collectors.toList());
    }

    private boolean matchesArea(DriverProfile driver, String area) {
        if (area == null || area.isBlank()) {
            return true;
        }
        String searchArea = area.trim().toLowerCase();

        if (driver.getServiceArea() != null &&
                driver.getServiceArea().toLowerCase().contains(searchArea)) {
            return true;
        }

        if (driver.getCurrentLocation() != null &&
                driver.getCurrentLocation().getAreaName() != null &&
                driver.getCurrentLocation().getAreaName().toLowerCase().contains(searchArea)) {
            return true;
        }

        return false;
    }

    private void validateOwnership(DriverProfile profile, String currentUserId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (currentUserId != null && !currentUserId.equals(profile.getAccountId())) {
            throw new UnauthorizedActionException("You are not authorized to modify another driver's data");
        }
    }
}
