package parkinglot.models;

import jakarta.persistence.*;
import parkinglot.constants.ParkingSpotType;
import parkinglot.constants.VehicleType;
import parkinglot.constants.ParkingTicketStatus;
import parkinglot.models.spots.ParkingSpot;
import parkinglot.models.vehicles.Vehicle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "parking_lot_id")
    private List<ParkingTicket> allTickets = new ArrayList<>();

    protected ParkingLot() {}

    public ParkingLot(String id, String name, Location address) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.parkingRate = new ParkingRate();
        this.floors = new ArrayList<>();
        this.allTickets = new ArrayList<>();
    }

    public void addParkingFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public boolean isFullForType(VehicleType vehicleType) {
        ParkingSpotType required = mapVehicleToSpotType(vehicleType);
        return floors.stream().noneMatch(f -> {
            return f.getSpots().stream().anyMatch(s -> s.getType() == required && s.isFree());
        });
    }

    public synchronized ParkingTicket vehicleEntry(Vehicle vehicle) {
        if (isFullForType(vehicle.getType())) {
            System.out.println("Parking lot is full for vehicle type: " + vehicle.getType());
            return null;
        }

        ParkingSpot assignedSpot = null;
        for (ParkingFloor floor : floors) {
            assignedSpot = floor.assignVehicleToSlot(vehicle);
            if (assignedSpot != null) {
                break;
            }
        }

        if (assignedSpot == null) return null;

        ParkingTicket ticket = new ParkingTicket(vehicle.getLicenseNumber(), assignedSpot.getNumber());
        vehicle.assignTicket(ticket);
        allTickets.add(ticket);

        return ticket;
    }

    public synchronized boolean vehicleExit(Vehicle vehicle) {
        ParkingTicket ticket = vehicle.getTicket();
        if (ticket == null || !ticket.isPaid()) {
            System.out.println("Invalid or unpaid ticket for vehicle: " + vehicle.getLicenseNumber());
            return false;
        }

        String spotNumber = ticket.getSpotNumber();
        for (ParkingFloor floor : floors) {
            Optional<ParkingSpot> spotOpt = floor.getSpots().stream()
                    .filter(s -> s.getNumber().equals(spotNumber))
                    .findFirst();
            if (spotOpt.isPresent()) {
                floor.freeSlot(spotOpt.get());
                break;
            }
        }

        ticket.setStatus(ParkingTicketStatus.COMPLETED);
        vehicle.setTicket(null);
        System.out.println("[EXIT] Vehicle " + vehicle.getLicenseNumber() + " exited successfully.");
        return true;
    }

    public double calculateFee(ParkingTicket ticket) {
        if (ticket == null || parkingRate == null) return 0.0;
        long duration = java.time.Duration.between(ticket.getIssuedAt(), java.time.LocalDateTime.now()).toMinutes();
        return parkingRate.calculateFee(duration);
    }

    public ParkingTicket findTicket(String ticketNumber) {
        return allTickets.stream()
                .filter(t -> t.getTicketNumber().equals(ticketNumber))
                .findFirst()
                .orElse(null);
    }

    private ParkingSpotType mapVehicleToSpotType(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORBIKE: return ParkingSpotType.MOTORBIKE;
            case ELECTRIC:  return ParkingSpotType.ELECTRIC;
            case TRUCK:
            case VAN:       return ParkingSpotType.LARGE;
            default:        return ParkingSpotType.COMPACT;
        }
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
    public List<ParkingTicket> getAllTickets() { return allTickets; }
}
