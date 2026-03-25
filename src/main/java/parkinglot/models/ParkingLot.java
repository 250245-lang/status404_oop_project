package parkinglot.models;

import jakarta.persistence.*;
import parkinglot.models.spots.ParkingSpot;
import parkinglot.models.vehicles.Vehicle;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ParkingLot {
    @Id
    private String id;
    private String name;

    @Embedded
    private Location address;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rate_id")
    private ParkingRate parkingRate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "parking_lot_id")
    private List<ParkingFloor> floors = new ArrayList<>();

    protected ParkingLot() {}

    public ParkingLot(String id, String name, Location address) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.parkingRate = new ParkingRate();
        this.floors = new ArrayList<>();
    }

    public void addParkingFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public ParkingSpot assignVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.assignVehicleToSlot(vehicle);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    // Getters / Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Location getAddress() { return address; }
    public void setAddress(Location address) { this.address = address; }
    public ParkingRate getParkingRate() { return parkingRate; }
    public void setParkingRate(ParkingRate parkingRate) { this.parkingRate = parkingRate; }
    public List<ParkingFloor> getFloors() { return floors; }
}
