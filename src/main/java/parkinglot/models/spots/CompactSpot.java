package parkinglot.models.spots;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.ParkingSpotType;
import parkinglot.constants.VehicleType;
import parkinglot.models.vehicles.Vehicle;

@Entity
@DiscriminatorValue("COMPACT")
public class CompactSpot extends ParkingSpot {
    protected CompactSpot(){super();}
    public CompactSpot(String number) {
        super(number, ParkingSpotType.COMPACT);
    }

    @Override
    public boolean assignVehicle(Vehicle vehicle) {
        if (vehicle.getType() == VehicleType.TRUCK) {
            System.out.println("Compact spot cannot accommodate " + vehicle.getType());
            return false;
        }
        return super.assignVehicle(vehicle);
    }
}