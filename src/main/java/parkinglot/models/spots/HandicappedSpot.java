package parkinglot.models.spots;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.ParkingSpotType;

@Entity
@DiscriminatorValue("HANDICAPPED")
public class HandicappedSpot extends ParkingSpot {
    protected HandicappedSpot(){super();}
    public HandicappedSpot(String number) {
        super(number, ParkingSpotType.HANDICAPPED);
    }
}