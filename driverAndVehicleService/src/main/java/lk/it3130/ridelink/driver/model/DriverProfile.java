package lk.it3130.ridelink.driver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "drivers")
public class DriverProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String accountId;

    private String licenseNumber;

    private Integer experienceYears;

    @Indexed
    private String serviceArea;

    @Indexed
    private AvailabilityStatus availability;

    private DriverAccountStatus status;

    private Vehicle vehicle;

    private Location currentLocation;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
