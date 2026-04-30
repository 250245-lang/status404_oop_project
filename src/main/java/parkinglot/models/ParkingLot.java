package parkinglot.models;

import jakarta.persistence.*;
import parkinglot.constants.ParkingSpotType;
import parkinglot.constants.VehicleType;
import parkinglot.hardware.EntrancePanel;
import parkinglot.hardware.ExitPanel;
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
    @JoinColumn(name = "parking_lot_id") // Creates a foreign key in the floor table
    private List<ParkingFloor> floors = new ArrayList<>();


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parking_lot_id")
    private List<EntrancePanel> entrancePanels = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parking_lot_id")
    private List<ExitPanel> exitPanels = new ArrayList<>();

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
        this.entrancePanels = new ArrayList<>();
        this.exitPanels = new ArrayList<>();
        this.allTickets = new ArrayList<>();
    }

    // ─── Floor management ────────────────────────────────────────────────────

    public void addParkingFloor(ParkingFloor floor) {
        floors.add(floor);
        System.out.println("Floor '" + floor.getName() + "' added to " + name);
    }

    public boolean removeParkingFloor(String floorName) {
        boolean removed = floors.removeIf(f -> f.getName().equals(floorName));
        if (removed) System.out.println("Floor '" + floorName + "' removed.");
        return removed;
    }

    // ─── Panel management ────────────────────────────────────────────────────

    public void addEntrancePanel(EntrancePanel panel) {
        entrancePanels.add(panel);
    }

    public void addExitPanel(ExitPanel panel) {
        exitPanels.add(panel);
    }

    // ─── Capacity checks ─────────────────────────────────────────────────────

    public boolean isFull() {
        return floors.stream().allMatch(f -> f.getFreeSpotCount() == 0);
    }

    public boolean isFullForType(VehicleType vehicleType) {
        ParkingSpotType required = mapVehicleToSpotType(vehicleType);
        return floors.stream().noneMatch(f -> f.hasAvailableSpot(required));
    }

    public long getTotalFreeSpots() {
        return floors.stream().mapToLong(ParkingFloor::getFreeSpotCount).sum();
    }

    // ─── Vehicle entry / exit ────────────────────────────────────────────────

    public ParkingTicket vehicleEntry(Vehicle vehicle) {
        if (isFullForType(vehicle.getType())) {
            System.out.println("Parking lot is full for vehicle type: " + vehicle.getType());
            entrancePanels.forEach(EntrancePanel::showFullMessage);
            return null;
        }

        ParkingSpot assignedSpot = null;
        ParkingFloor assignedFloor = null;

        for (ParkingFloor floor : floors) {
            assignedSpot = floor.assignVehicleToSlot(vehicle);
            if (assignedSpot != null) {
                assignedFloor = floor;
                break;
            }
        }

        if (assignedSpot == null) {
            System.out.println("Could not find a spot for vehicle: " + vehicle.getLicenseNumber());
            return null;
        }

        ParkingTicket ticket = new ParkingTicket(vehicle.getLicenseNumber(), assignedSpot.getNumber());
        vehicle.assignTicket(ticket);
        allTickets.add(ticket);

        System.out.printf("[ENTRY] Vehicle %s -> Floor '%s', Spot '%s', Ticket '%s'%n",
                vehicle.getLicenseNumber(), assignedFloor.getName(),
                assignedSpot.getNumber(), ticket.getTicketNumber());

        return ticket;
    }

    /**
     * Processes a vehicle's exit:
     *   1. Verifies ticket is paid
     *   2. Frees the spot
     *   3. Removes ticket from active list
     */
    public boolean vehicleExit(Vehicle vehicle) {
        ParkingTicket ticket = vehicle.getTicket();
        if (ticket == null) {
            System.out.println("No ticket associated with vehicle: " + vehicle.getLicenseNumber());
            return false;
        }
        if (!ticket.isPaid()) {
            System.out.println("Ticket " + ticket.getTicketNumber() + " is not paid yet. Cannot exit.");
            return false;
        }

        // Find and free the spot
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

        ticket.setStatus(parkinglot.constants.ParkingTicketStatus.COMPLETED);

        vehicle.setTicket(null);
        System.out.println("[EXIT] Vehicle " + vehicle.getLicenseNumber() + " exited. Spot " + spotNumber + " is now free.");
        return true;
    }

    // ─── Display ─────────────────────────────────────────────────────────────

    public void showStatus() {
        System.out.println("\n========== " + name + " Status ==========");
        System.out.println("Address: " + address);
        System.out.println("Total free spots: " + getTotalFreeSpots());
        System.out.println("Full: " + isFull());
        System.out.println("Active tickets: " + allTickets.size());
        floors.forEach(f -> System.out.println("  " + f));
        System.out.println("=========================================\n");
    }

    // ─── Ticket lookup ───────────────────────────────────────────────────────

    public ParkingTicket findTicket(String ticketNumber) {
        return allTickets.stream()
                .filter(t -> t.getTicketNumber().equals(ticketNumber))
                .findFirst()
                .orElse(null);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ParkingSpotType mapVehicleToSpotType(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORBIKE: return ParkingSpotType.MOTORBIKE;
            case ELECTRIC:  return ParkingSpotType.ELECTRIC;
            case TRUCK:
            case VAN:       return ParkingSpotType.LARGE;
            default:        return ParkingSpotType.COMPACT;
        }
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getAddress() { return address; }
    public void setAddress(Location address) { this.address = address; }

    public ParkingRate getParkingRate() { return parkingRate; }
    public void setParkingRate(ParkingRate parkingRate) { this.parkingRate = parkingRate; }

    public List<ParkingFloor> getFloors() { return floors; }
    public List<EntrancePanel> getEntrancePanels() { return entrancePanels; }
    public List<ExitPanel> getExitPanels() { return exitPanels; }
    public List<ParkingTicket> getActiveTickets() {
        return allTickets.stream()
                .filter(t -> t.getStatus() == parkinglot.constants.ParkingTicketStatus.ACTIVE || t.getStatus() == parkinglot.constants.ParkingTicketStatus.PAID)
                .collect(java.util.stream.Collectors.toList());
    }
    public List<ParkingTicket> getAllTickets() { return allTickets; }
}