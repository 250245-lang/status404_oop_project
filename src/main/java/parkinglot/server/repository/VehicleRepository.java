package parkinglot.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import parkinglot.models.vehicles.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
}
