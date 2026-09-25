package lk.it3130.ridelink.driver.repository;

import lk.it3130.ridelink.driver.model.AvailabilityStatus;
import lk.it3130.ridelink.driver.model.DriverAccountStatus;
import lk.it3130.ridelink.driver.model.DriverProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository extends MongoRepository<DriverProfile, String> {

    Optional<DriverProfile> findByAccountId(String accountId);

    boolean existsByAccountId(String accountId);

    List<DriverProfile> findByAvailabilityAndStatus(AvailabilityStatus availability, DriverAccountStatus status);

    List<DriverProfile> findByAvailabilityAndStatusAndServiceAreaIgnoreCase(
            AvailabilityStatus availability,
            DriverAccountStatus status,
            String serviceArea
    );
}
