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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @InjectMocks
    private DriverService driverService;

    private DriverProfile sampleDriver;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleVehicle = Vehicle.builder()
                .make("Toyota")
                .model("Prius")
                .licensePlate("WP-CAB-1234")
                .capacity(4)
                .vehicleType(VehicleType.SEDAN)
                .color("Pearl White")
                .build();

        sampleDriver = DriverProfile.builder()
                .id("driver-1")
                .accountId("acc-123")
                .licenseNumber("B9876543")
                .experienceYears(5)
                .serviceArea("Colombo")
                .availability(AvailabilityStatus.OFFLINE)
                .status(DriverAccountStatus.ACTIVE)
                .vehicle(sampleVehicle)
                .currentLocation(Location.builder()
                        .latitude(6.9271)
                        .longitude(79.8612)
                        .areaName("Colombo 03")
                        .updatedAt(Instant.now())
                        .build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Create Profile Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create driver profile successfully")
        void testCreateProfile_Success() {
            CreateDriverProfileRequest request = CreateDriverProfileRequest.builder()
                    .licenseNumber("B9876543")
                    .experienceYears(3)
                    .serviceArea("Colombo")
                    .build();

            when(driverProfileRepository.existsByAccountId("acc-123")).thenReturn(false);
            when(driverProfileRepository.save(any(DriverProfile.class))).thenReturn(sampleDriver);

            DriverResponse response = driverService.createProfile("acc-123", request, "acc-123", false);

            assertThat(response).isNotNull();
            assertThat(response.getAccountId()).isEqualTo("acc-123");
            assertThat(response.getLicenseNumber()).isEqualTo("B9876543");
            verify(driverProfileRepository).save(any(DriverProfile.class));
        }

        @Test
        @DisplayName("Negative Scenario: Should throw DriverAlreadyExistsException when account already has driver profile")
        void testCreateProfile_Duplicate_ThrowsException() {
            CreateDriverProfileRequest request = CreateDriverProfileRequest.builder()
                    .licenseNumber("B9876543")
                    .experienceYears(3)
                    .serviceArea("Colombo")
                    .build();

            when(driverProfileRepository.existsByAccountId("acc-123")).thenReturn(true);

            assertThatThrownBy(() -> driverService.createProfile("acc-123", request, "acc-123", false))
                    .isInstanceOf(DriverAlreadyExistsException.class)
                    .hasMessageContaining("Driver operational profile already exists for account ID");

            verify(driverProfileRepository, never()).save(any());
        }

        @Test
        @DisplayName("Negative Scenario: Should throw UnauthorizedActionException when user creates profile for another account")
        void testCreateProfile_Unauthorized_ThrowsException() {
            CreateDriverProfileRequest request = CreateDriverProfileRequest.builder()
                    .licenseNumber("B9876543")
                    .experienceYears(3)
                    .serviceArea("Colombo")
                    .build();

            assertThatThrownBy(() -> driverService.createProfile("acc-123", request, "different-user", false))
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessageContaining("not authorized to create a driver profile for another account");

            verify(driverProfileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Profile Tests")
    class RetrieveProfileTests {

        @Test
        @DisplayName("Should retrieve driver profile by driver ID")
        void testGetDriverById_Success() {
            when(driverProfileRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));

            DriverResponse response = driverService.getDriverById("driver-1");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("driver-1");
            assertThat(response.getServiceArea()).isEqualTo("Colombo");
        }

        @Test
        @DisplayName("Negative Scenario: Should throw DriverNotFoundException when driver not found by ID")
        void testGetDriverById_NotFound_ThrowsException() {
            when(driverProfileRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> driverService.getDriverById("non-existent"))
                    .isInstanceOf(DriverNotFoundException.class)
                    .hasMessageContaining("Driver profile not found with ID");
        }

        @Test
        @DisplayName("Should retrieve driver profile by account ID")
        void testGetDriverByAccountId_Success() {
            when(driverProfileRepository.findByAccountId("acc-123")).thenReturn(Optional.of(sampleDriver));

            DriverResponse response = driverService.getDriverByAccountId("acc-123");

            assertThat(response).isNotNull();
            assertThat(response.getAccountId()).isEqualTo("acc-123");
        }
    }

    @Nested
    @DisplayName("Update Vehicle Tests")
    class UpdateVehicleTests {

        @Test
        @DisplayName("Should update vehicle details successfully")
        void testUpdateVehicle_Success() {
            UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                    .make("Honda")
                    .model("Vezel")
                    .licensePlate("WP-CAD-7890")
                    .capacity(5)
                    .vehicleType(VehicleType.SUV)
                    .color("Black")
                    .build();

            when(driverProfileRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
            when(driverProfileRepository.save(any(DriverProfile.class))).thenAnswer(i -> i.getArgument(0));

            DriverResponse response = driverService.updateVehicle("driver-1", request, "acc-123", false);

            assertThat(response).isNotNull();
            assertThat(response.getVehicle().getMake()).isEqualTo("Honda");
            assertThat(response.getVehicle().getModel()).isEqualTo("Vezel");
            assertThat(response.getVehicle().getLicensePlate()).isEqualTo("WP-CAD-7890");
            assertThat(response.getVehicle().getCapacity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Negative Scenario: Should throw UnauthorizedActionException when modifying another driver's vehicle")
        void testUpdateVehicle_Unauthorized_ThrowsException() {
            UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                    .make("Honda")
                    .model("Vezel")
                    .licensePlate("WP-CAD-7890")
                    .capacity(5)
                    .vehicleType(VehicleType.SUV)
                    .color("Black")
                    .build();

            when(driverProfileRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));

            assertThatThrownBy(() -> driverService.updateVehicle("driver-1", request, "intruder-user", false))
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessageContaining("not authorized to modify another driver's data");

            verify(driverProfileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Availability Tests")
    class UpdateAvailabilityTests {

        @Test
        @DisplayName("Should update availability to AVAILABLE when vehicle is assigned")
        void testUpdateAvailability_ToAvailable_Success() {
            UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                    .availability(AvailabilityStatus.AVAILABLE)
                    .build();

            when(driverProfileRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
            when(driverProfileRepository.save(any(DriverProfile.class))).thenAnswer(i -> i.getArgument(0));

            DriverResponse response = driverService.updateAvailability("driver-1", request, "acc-123", false);

            assertThat(response).isNotNull();
            assertThat(response.getAvailability()).isEqualTo(AvailabilityStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Negative Scenario: Should throw DriverNotEligibleException when setting AVAILABLE without vehicle")
        void testUpdateAvailability_WithoutVehicle_ThrowsException() {
            sampleDriver.setVehicle(null);
            UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                    .availability(AvailabilityStatus.AVAILABLE)
                    .build();

            when(driverProfileRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));

            assertThatThrownBy(() -> driverService.updateAvailability("driver-1", request, "acc-123", false))
                    .isInstanceOf(DriverNotEligibleException.class)
                    .hasMessageContaining("Cannot set driver availability to AVAILABLE without an assigned vehicle");

            verify(driverProfileRepository, never()).save(any());
        }

        @Test
        @DisplayName("Negative Scenario: Should throw DriverNotEligibleException when driver is suspended")
        void testUpdateAvailability_SuspendedDriver_ThrowsException() {
            sampleDriver.setStatus(DriverAccountStatus.SUSPENDED);
            UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                    .availability(AvailabilityStatus.AVAILABLE)
                    .build();

            when(driverProfileRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));

            assertThatThrownBy(() -> driverService.updateAvailability("driver-1", request, "acc-123", false))
                    .isInstanceOf(DriverNotEligibleException.class)
                    .hasMessageContaining("Cannot update availability for suspended driver");

            verify(driverProfileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Location Tests")
    class UpdateLocationTests {

        @Test
        @DisplayName("Should update simulated GPS coordinates and area")
        void testUpdateLocation_Success() {
            UpdateLocationRequest request = UpdateLocationRequest.builder()
                    .latitude(6.9015)
                    .longitude(79.8550)
                    .areaName("Colombo 04 - Bambalapitiya")
                    .build();

            when(driverProfileRepository.findById("driver-1")).thenReturn(Optional.of(sampleDriver));
            when(driverProfileRepository.save(any(DriverProfile.class))).thenAnswer(i -> i.getArgument(0));

            DriverResponse response = driverService.updateLocation("driver-1", request, "acc-123", false);

            assertThat(response).isNotNull();
            assertThat(response.getCurrentLocation().getLatitude()).isEqualTo(6.9015);
            assertThat(response.getCurrentLocation().getLongitude()).isEqualTo(79.8550);
            assertThat(response.getCurrentLocation().getAreaName()).isEqualTo("Colombo 04 - Bambalapitiya");
        }
    }

    @Nested
    @DisplayName("Eligible Available Drivers Interservice Tests")
    class EligibleAvailableDriversTests {

        @Test
        @DisplayName("Should return eligible available drivers matching area")
        void testGetEligibleAvailableDrivers_AreaMatch_Success() {
            sampleDriver.setAvailability(AvailabilityStatus.AVAILABLE);
            when(driverProfileRepository.findByAvailabilityAndStatus(AvailabilityStatus.AVAILABLE, DriverAccountStatus.ACTIVE))
                    .thenReturn(List.of(sampleDriver));

            List<AvailableDriverResponse> results = driverService.getEligibleAvailableDrivers("Colombo", null, null);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getDriverId()).isEqualTo("driver-1");
            assertThat(results.get(0).getAccountId()).isEqualTo("acc-123");
            assertThat(results.get(0).getVehicle().getModel()).isEqualTo("Prius");
        }

        @Test
        @DisplayName("Should filter by minimum passenger capacity")
        void testGetEligibleAvailableDrivers_MinCapacityFilter() {
            sampleDriver.setAvailability(AvailabilityStatus.AVAILABLE);
            sampleDriver.getVehicle().setCapacity(4);

            when(driverProfileRepository.findByAvailabilityAndStatus(AvailabilityStatus.AVAILABLE, DriverAccountStatus.ACTIVE))
                    .thenReturn(List.of(sampleDriver));

            List<AvailableDriverResponse> matching = driverService.getEligibleAvailableDrivers("Colombo", 4, null);
            assertThat(matching).hasSize(1);

            List<AvailableDriverResponse> notMatching = driverService.getEligibleAvailableDrivers("Colombo", 6, null);
            assertThat(notMatching).isEmpty();
        }

        @Test
        @DisplayName("Negative Scenario: Should return empty list when no drivers available in requested area")
        void testGetEligibleAvailableDrivers_NoAvailableDriver_ReturnsEmpty() {
            sampleDriver.setAvailability(AvailabilityStatus.AVAILABLE);

            when(driverProfileRepository.findByAvailabilityAndStatus(AvailabilityStatus.AVAILABLE, DriverAccountStatus.ACTIVE))
                    .thenReturn(List.of(sampleDriver));

            List<AvailableDriverResponse> results = driverService.getEligibleAvailableDrivers("Kandy", null, null);

            assertThat(results).isEmpty();
        }
    }
}
