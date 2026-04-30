package parkinglot.models.spots;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import parkinglot.constants.ParkingSpotType;
import parkinglot.models.vehicles.Vehicle;
import jakarta.persistence.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompactSpot.class, name = "COMPACT"),
        @JsonSubTypes.Type(value = LargeSpot.class, name = "LARGE"),
        @JsonSubTypes.Type(value = MotorbikeSpot.class, name = "MOTORBIKE"),
        @JsonSubTypes.Type(value = ElectricSpot.class, name = "ELECTRIC"),
        @JsonSubTypes.Type(value = HandicappedSpot.class, name = "HANDICAPPED")
})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "spot_category")         // Column to tell types apart
public abstract class ParkingSpot {

    @Id
    private String number;

    private boolean free;

    @Enumerated(EnumType.STRING)
    private ParkingSpotType type;

    // One-to-One relationship with Vehicle
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
            System.out.println("Spot " + number + " is already occupied.");
            return false;
        }
        this.currentVehicle = vehicle;
        this.free = false;
        System.out.println("Vehicle " + vehicle.getLicenseNumber() + " assigned to spot " + number);
        return true;
    }

    public boolean removeVehicle() {
        if (free) {
            System.out.println("Spot " + number + " is already empty.");
            return false;
        }
        System.out.println("Vehicle " + currentVehicle.getLicenseNumber() + " removed from spot " + number);
        this.currentVehicle = null;
        this.free = true;
        return true;
    }

    // Getters and Setters
    public boolean getIsFree() { return free; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public ParkingSpotType getType() { return type; }

    public Vehicle getCurrentVehicle() { return currentVehicle; }

}