package parkinglot.models.spots;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.ParkingSpotType;
import parkinglot.constants.VehicleType;
import parkinglot.models.vehicles.Vehicle;

@Entity
@DiscriminatorValue("ELECTRIC")
public class ElectricSpot extends ParkingSpot {

    protected ElectricSpot(){super();}

    public ElectricSpot(String number) {
        super(number, ParkingSpotType.ELECTRIC);
    }

    @Override
    public boolean assignVehicle(Vehicle vehicle) {
        if (vehicle.getType() != VehicleType.ELECTRIC) {
            System.out.println("Electric spot is reserved for electric vehicles only. Received: " + vehicle.getType());
            return false;
        }
        return super.assignVehicle(vehicle);
    }
}