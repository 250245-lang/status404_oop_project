package parkinglot.models.spots;

import parkinglot.constants.ParkingSpotType;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "spot_category")
public abstract class ParkingSpot {

    @Id
    private String number;

    private boolean free;

    @Enumerated(EnumType.STRING)
    private ParkingSpotType type;

    protected ParkingSpot() {}

    public ParkingSpot(String number, ParkingSpotType type) {
        this.number = number;
        this.type = type;
        this.free = true;
    }

    public boolean isFree() { return free; }

    // Placeholder for assignVehicle until Vehicle class is implemented
    public void setFree(boolean free) {
        this.free = free;
    }

    // Getters and Setters
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public ParkingSpotType getType() { return type; }
    public void setType(ParkingSpotType type) { this.type = type; }
}
