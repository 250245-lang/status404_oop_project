package parkinglot.models.spots;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.ParkingSpotType;

@Entity
@DiscriminatorValue("MOTORBIKE")
public class MotorbikeSpot extends ParkingSpot {
    protected MotorbikeSpot(){super();}
    public MotorbikeSpot(String number) {
        super(number, ParkingSpotType.MOTORBIKE);
    }
}