package parkinglot.models.spots;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.ParkingSpotType;

@Entity
@DiscriminatorValue("COMPACT")
public class CompactSpot extends ParkingSpot {
    protected CompactSpot(){super();}
    public CompactSpot(String number) {
        super(number, ParkingSpotType.COMPACT);
    }
}