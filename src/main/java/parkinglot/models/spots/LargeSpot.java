package parkinglot.models.spots;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.ParkingSpotType;

@Entity
@DiscriminatorValue("LARGE")
public class LargeSpot extends ParkingSpot {
    protected LargeSpot(){super();}
    public LargeSpot(String number) {
        super(number, ParkingSpotType.LARGE);
    }
}