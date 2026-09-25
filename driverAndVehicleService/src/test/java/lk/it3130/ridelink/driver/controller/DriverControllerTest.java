package lk.it3130.ridelink.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.it3130.ridelink.driver.dto.AvailableDriverResponse;
import lk.it3130.ridelink.driver.dto.CreateDriverProfileRequest;
import lk.it3130.ridelink.driver.dto.DriverResponse;
import lk.it3130.ridelink.driver.dto.UpdateAvailabilityRequest;
import lk.it3130.ridelink.driver.dto.UpdateLocationRequest;
import lk.it3130.ridelink.driver.dto.UpdateVehicleRequest;
import lk.it3130.ridelink.driver.dto.VehicleDto;
import lk.it3130.ridelink.driver.exception.DriverAlreadyExistsException;
import lk.it3130.ridelink.driver.exception.DriverNotEligibleException;
import lk.it3130.ridelink.driver.exception.DriverNotFoundException;
import lk.it3130.ridelink.driver.exception.GlobalExceptionHandler;
import lk.it3130.ridelink.driver.model.AvailabilityStatus;
import lk.it3130.ridelink.driver.model.DriverAccountStatus;
import lk.it3130.ridelink.driver.model.VehicleType;
import lk.it3130.ridelink.driver.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DriverResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(driverController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = DriverResponse.builder()
                .id("driver-1")
                .accountId("acc-123")
                .licenseNumber("B9876543")
                .experienceYears(4)
                .serviceArea("Colombo")
                .availability(AvailabilityStatus.OFFLINE)
                .status(DriverAccountStatus.ACTIVE)
                .vehicle(VehicleDto.builder()
                        .make("Toyota")
                        .model("Axio")
                        .licensePlate("WP-CAA-5678")
                        .capacity(4)
                        .vehicleType(VehicleType.SEDAN)
                        .color("Silver")
                        .build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("POST /drivers/{accountId}/profile - Success creates profile (201)")
    void testCreateProfile_Success() throws Exception {
        CreateDriverProfileRequest request = CreateDriverProfileRequest.builder()
                .licenseNumber("B9876543")
                .experienceYears(4)
                .serviceArea("Colombo")
                .build();

        when(driverService.createProfile(eq("acc-123"), any(CreateDriverProfileRequest.class), any(), eq(false)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/drivers/acc-123/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("driver-1"))
                .andExpect(jsonPath("$.accountId").value("acc-123"))
                .andExpect(jsonPath("$.serviceArea").value("Colombo"));
    }

    @Test
    @DisplayName("Negative Scenario: POST /drivers/{accountId}/profile - Validation failure returns 400")
    void testCreateProfile_ValidationFailure() throws Exception {
        CreateDriverProfileRequest invalidRequest = CreateDriverProfileRequest.builder()
                .licenseNumber("") // Blank: violates @NotBlank
                .serviceArea("")    // Blank: violates @NotBlank
                .build();

        mockMvc.perform(post("/drivers/acc-123/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors", hasKey("licenseNumber")))
                .andExpect(jsonPath("$.validationErrors", hasKey("serviceArea")));
    }

    @Test
    @DisplayName("Negative Scenario: POST /drivers/{accountId}/profile - Duplicate profile returns 409")
    void testCreateProfile_Duplicate_Returns409() throws Exception {
        CreateDriverProfileRequest request = CreateDriverProfileRequest.builder()
                .licenseNumber("B9876543")
                .experienceYears(4)
                .serviceArea("Colombo")
                .build();

        when(driverService.createProfile(eq("acc-123"), any(), any(), eq(false)))
                .thenThrow(new DriverAlreadyExistsException("Driver operational profile already exists for account ID: acc-123"));

        mockMvc.perform(post("/drivers/acc-123/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("Driver operational profile already exists")));
    }

    @Test
    @DisplayName("GET /drivers/{driverId} - Success returns profile (200)")
    void testGetDriverById_Success() throws Exception {
        when(driverService.getDriverById("driver-1")).thenReturn(sampleResponse);

        mockMvc.perform(get("/drivers/driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("driver-1"))
                .andExpect(jsonPath("$.licenseNumber").value("B9876543"));
    }

    @Test
    @DisplayName("Negative Scenario: GET /drivers/{driverId} - Not found returns 404")
    void testGetDriverById_NotFound() throws Exception {
        when(driverService.getDriverById("unknown-id"))
                .thenThrow(new DriverNotFoundException("Driver profile not found with ID: unknown-id"));

        mockMvc.perform(get("/drivers/unknown-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Driver profile not found")));
    }

    @Test
    @DisplayName("PUT /drivers/{driverId}/vehicle - Success updates vehicle (200)")
    void testUpdateVehicle_Success() throws Exception {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .make("Toyota")
                .model("Axio")
                .licensePlate("WP-CAA-5678")
                .capacity(4)
                .vehicleType(VehicleType.SEDAN)
                .color("Silver")
                .build();

        when(driverService.updateVehicle(eq("driver-1"), any(), any(), eq(false)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/drivers/driver-1/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicle.make").value("Toyota"))
                .andExpect(jsonPath("$.vehicle.licensePlate").value("WP-CAA-5678"));
    }

    @Test
    @DisplayName("Negative Scenario: PUT /drivers/{driverId}/vehicle - Zero/negative capacity returns 400")
    void testUpdateVehicle_InvalidCapacity() throws Exception {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .make("Toyota")
                .model("Axio")
                .licensePlate("WP-CAA-5678")
                .capacity(0) // Minimum is 1
                .vehicleType(VehicleType.SEDAN)
                .color("Silver")
                .build();

        mockMvc.perform(put("/drivers/driver-1/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.capacity").exists());
    }

    @Test
    @DisplayName("PATCH /drivers/{driverId}/availability - Success updates availability (200)")
    void testUpdateAvailability_Success() throws Exception {
        UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                .availability(AvailabilityStatus.AVAILABLE)
                .build();

        sampleResponse.setAvailability(AvailabilityStatus.AVAILABLE);
        when(driverService.updateAvailability(eq("driver-1"), any(), any(), eq(false)))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/drivers/driver-1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availability").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Negative Scenario: PATCH /drivers/{driverId}/availability - Ineligible driver returns 400")
    void testUpdateAvailability_NotEligible() throws Exception {
        UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                .availability(AvailabilityStatus.AVAILABLE)
                .build();

        when(driverService.updateAvailability(eq("driver-1"), any(), any(), eq(false)))
                .thenThrow(new DriverNotEligibleException("Cannot set driver availability to AVAILABLE without an assigned vehicle"));

        mockMvc.perform(patch("/drivers/driver-1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("without an assigned vehicle")));
    }

    @Test
    @DisplayName("PATCH /drivers/{driverId}/location - Success updates location (200)")
    void testUpdateLocation_Success() throws Exception {
        UpdateLocationRequest request = UpdateLocationRequest.builder()
                .latitude(6.9271)
                .longitude(79.8612)
                .areaName("Colombo Fort")
                .build();

        when(driverService.updateLocation(eq("driver-1"), any(), any(), eq(false)))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/drivers/driver-1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /drivers/available - Returns eligible available drivers (200)")
    void testGetEligibleAvailableDrivers_Success() throws Exception {
        AvailableDriverResponse availableDriver = AvailableDriverResponse.builder()
                .driverId("driver-1")
                .accountId("acc-123")
                .serviceArea("Colombo")
                .availability(AvailabilityStatus.AVAILABLE)
                .vehicle(VehicleDto.builder()
                        .make("Toyota")
                        .model("Axio")
                        .licensePlate("WP-CAA-5678")
                        .capacity(4)
                        .vehicleType(VehicleType.SEDAN)
                        .build())
                .build();

        when(driverService.getEligibleAvailableDrivers("Colombo", 4, VehicleType.SEDAN))
                .thenReturn(List.of(availableDriver));

        mockMvc.perform(get("/drivers/available")
                        .param("area", "Colombo")
                        .param("minCapacity", "4")
                        .param("vehicleType", "SEDAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].driverId").value("driver-1"))
                .andExpect(jsonPath("$[0].vehicle.licensePlate").value("WP-CAA-5678"));
    }
}
