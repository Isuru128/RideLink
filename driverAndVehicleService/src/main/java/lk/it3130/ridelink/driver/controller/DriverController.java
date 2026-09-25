package lk.it3130.ridelink.driver.controller;

import jakarta.validation.Valid;
import lk.it3130.ridelink.driver.dto.AvailableDriverResponse;
import lk.it3130.ridelink.driver.dto.CreateDriverProfileRequest;
import lk.it3130.ridelink.driver.dto.DriverResponse;
import lk.it3130.ridelink.driver.dto.UpdateAvailabilityRequest;
import lk.it3130.ridelink.driver.dto.UpdateLocationRequest;
import lk.it3130.ridelink.driver.dto.UpdateVehicleRequest;
import lk.it3130.ridelink.driver.model.VehicleType;
import lk.it3130.ridelink.driver.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/{accountId}/profile")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DriverResponse> createProfile(
            @PathVariable String accountId,
            @Valid @RequestBody CreateDriverProfileRequest request,
            Authentication authentication) {

        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = isAdmin(authentication);

        DriverResponse response = driverService.createProfile(accountId, request, currentUserId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable String driverId) {
        DriverResponse response = driverService.getDriverById(driverId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<DriverResponse> getDriverByAccountId(@PathVariable String accountId) {
        DriverResponse response = driverService.getDriverByAccountId(accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{driverId}/vehicle")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DriverResponse> updateVehicle(
            @PathVariable String driverId,
            @Valid @RequestBody UpdateVehicleRequest request,
            Authentication authentication) {

        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = isAdmin(authentication);

        DriverResponse response = driverService.updateVehicle(driverId, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{driverId}/availability")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DriverResponse> updateAvailability(
            @PathVariable String driverId,
            @Valid @RequestBody UpdateAvailabilityRequest request,
            Authentication authentication) {

        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = isAdmin(authentication);

        DriverResponse response = driverService.updateAvailability(driverId, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{driverId}/location")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable String driverId,
            @Valid @RequestBody UpdateLocationRequest request,
            Authentication authentication) {

        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = isAdmin(authentication);

        DriverResponse response = driverService.updateLocation(driverId, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableDriverResponse>> getEligibleAvailableDrivers(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) VehicleType vehicleType) {

        List<AvailableDriverResponse> drivers = driverService.getEligibleAvailableDrivers(area, minCapacity, vehicleType);
        return ResponseEntity.ok(drivers);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
