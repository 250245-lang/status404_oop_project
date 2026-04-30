package parkinglot.models;

import parkinglot.constants.ParkingSpotType;
import parkinglot.constants.VehicleType;
import parkinglot.hardware.ParkingDisplayBoard;
import parkinglot.models.spots.*;
import parkinglot.models.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.*;

@Entity
public class ParkingFloor {

    @Id
    private String name;

    // One floor has many spots.
    // We use EAGER so that when you load the floor, you can immediately see which spots are free.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "floor_id")
    private List<ParkingSpot> spots = new ArrayList<>();

    @Transient
    private ParkingDisplayBoard displayBoard;

    protected ParkingFloor() {}

    public ParkingFloor(String name) {
        this.name = name;
        this.spots = new ArrayList<>();
        this.displayBoard = new ParkingDisplayBoard("DisplayBoard-" + name);
    }

    public void addParkingSlot(ParkingSpot spot) {
        spots.add(spot);
        System.out.println("Spot " + spot.getNumber() + " (" + spot.getType() + ") added to floor " + name);
    }

    public ParkingSpot assignVehicleToSlot(Vehicle vehicle) {
        ParkingSpotType required = getRequiredSpotType(vehicle.getType());

        ParkingSpot spot = spots.stream()
                .filter(s -> s.getType() == required && s.isFree())
                .findFirst()
                .orElse(null);

        if (spot == null) {
            System.out.println("No free " + required + " spot available on floor " + name);
            return null;
        }

        boolean assigned = spot.assignVehicle(vehicle);
        if (assigned) {
            return spot;
        }
        return null;
    }

    public boolean freeSlot(ParkingSpot spot) {
        boolean removed = spot.removeVehicle();
        return removed;
    }

    private ParkingSpotType getRequiredSpotType(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORBIKE: return ParkingSpotType.MOTORBIKE;
            case ELECTRIC:  return ParkingSpotType.ELECTRIC;
            case TRUCK:
            case VAN:       return ParkingSpotType.LARGE;
            default:        return ParkingSpotType.COMPACT;
        }
    }

    public boolean hasAvailableSpot(ParkingSpotType type) {
        return spots.stream().anyMatch(s -> s.getType() == type && s.isFree());
    }

    // Getters and Setters
    public int getTotalSpots() { return spots.size(); }

    public long getFreeSpotCount() {
        return spots.stream().filter(ParkingSpot::isFree).count();
    }

    public List<ParkingSpot> getSpots() { return spots; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ParkingDisplayBoard getDisplayBoard() { return displayBoard; }

}