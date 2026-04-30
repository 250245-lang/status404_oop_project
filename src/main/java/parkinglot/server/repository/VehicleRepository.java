package parkinglot.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import parkinglot.models.vehicles.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {
}
