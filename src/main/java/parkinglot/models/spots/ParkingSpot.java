package parkinglot.models.spots;

import parkinglot.constants.ParkingSpotType;
import parkinglot.models.vehicles.Vehicle;
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_id")
    private Vehicle currentVehicle;

    protected ParkingSpot() {}

    public ParkingSpot(String number, ParkingSpotType type) {
        this.number = number;
        this.type = type;
        this.free = true;
        this.currentVehicle = null;
    }

    public boolean isFree() { return free; }

    public boolean assignVehicle(Vehicle vehicle) {
        if (!free) {
            return false;
        }
        this.currentVehicle = vehicle;
        this.free = false;
        return true;
    }

    public boolean removeVehicle() {
        if (free) {
            return false;
        }
        this.currentVehicle = null;
        this.free = true;
        return true;
    }

    // Getters and Setters
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public ParkingSpotType getType() { return type; }
    public void setType(ParkingSpotType type) { this.type = type; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }
    public void setCurrentVehicle(Vehicle vehicle) { this.currentVehicle = vehicle; }
}
