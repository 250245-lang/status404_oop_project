package parkinglot.models.spots;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.ParkingSpotType;
import parkinglot.constants.VehicleType;
import parkinglot.models.vehicles.Vehicle;


@Entity
@DiscriminatorValue("MOTORBIKE")
public class MotorbikeSpot extends ParkingSpot {

    protected MotorbikeSpot() {super();}

    public MotorbikeSpot(String number) {
        super(number, ParkingSpotType.MOTORBIKE);
    }

    @Override
    public boolean assignVehicle(Vehicle vehicle) {
        if (vehicle.getType() != VehicleType.MOTORBIKE) {
            System.out.println("Motorbike spot only accepts motorcycles. Received: " + vehicle.getType());
            return false;
        }
        return super.assignVehicle(vehicle);
    }
}