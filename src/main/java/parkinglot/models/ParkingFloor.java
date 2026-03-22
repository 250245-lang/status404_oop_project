package parkinglot.models;

import parkinglot.models.spots.ParkingSpot;
import parkinglot.models.vehicles.Vehicle;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
public class ParkingFloor {

    @Id
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "floor_id")
    private List<ParkingSpot> spots = new ArrayList<>();

    protected ParkingFloor() {}

    public ParkingFloor(String name) {
        this.name = name;
    }

    public void addParkingSlot(ParkingSpot spot) {
        spots.add(spot);
    }

    public ParkingSpot assignVehicleToSlot(Vehicle vehicle) {
        // Simple 'First Available Spot' logic for now
        ParkingSpot spot = spots.stream()
                .filter(ParkingSpot::isFree)
                .findFirst()
                .orElse(null);

        if (spot != null) {
            spot.assignVehicle(vehicle);
        }
        return spot;
    }

    public boolean freeSlot(ParkingSpot spot) {
        return spot.removeVehicle();
    }

    public List<ParkingSpot> getSpots() { return spots; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
