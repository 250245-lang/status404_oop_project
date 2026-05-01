package parkinglot.hardware;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import parkinglot.models.ParkingTicket;
import parkinglot.models.vehicles.Vehicle;

@Entity
public class EntrancePanel {
    @Id
    private String id;

    public EntrancePanel(String id) {
        this.id = id;
    }

    protected EntrancePanel() {}

    public ParkingTicket printTicket(Vehicle vehicle, String assignedSpotNumber) {
        ParkingTicket ticket = new ParkingTicket(vehicle.getLicenseNumber(), assignedSpotNumber);
        vehicle.assignTicket(ticket);
        System.out.println("[EntrancePanel " + id + "] Printed ticket: " + ticket.getTicketNumber()
                + " for vehicle: " + vehicle.getLicenseNumber()
                + " -> Spot: " + assignedSpotNumber);
        return ticket;
    }

    public void showFullMessage() {
        System.out.println("[EntrancePanel " + id + "] *** PARKING LOT IS FULL. No entry allowed. ***");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

}