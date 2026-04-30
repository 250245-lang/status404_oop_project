package parkinglot.models.vehicles;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import parkinglot.constants.VehicleType;
import parkinglot.models.ParkingTicket;
import jakarta.persistence.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Car.class, name = "CAR"),
        @JsonSubTypes.Type(value = Motorbike.class, name = "MOTORBIKE"),
        @JsonSubTypes.Type(value = Van.class, name = "VAN"),
        @JsonSubTypes.Type(value = Truck.class, name = "TRUCK"),
        @JsonSubTypes.Type(value = ElectricVehicle.class, name = "ELECTRIC")
})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_category")
public abstract class Vehicle {
    @Id
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    // Relationship to the Ticket
    // cascade = ALL means if you save the Vehicle, the ticket is saved too.
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ticket_id")
    private ParkingTicket ticket;

    protected Vehicle() {}

    public Vehicle(String licenseNumber, VehicleType type) {
        this.licenseNumber = licenseNumber;
        this.type = type;
    }

    public void assignTicket(ParkingTicket ticket) {
        this.ticket = ticket;
        System.out.println("Ticket " + ticket.getTicketNumber() + " assigned to vehicle " + licenseNumber);
    }

    // Getters and Setters
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public VehicleType getType() { return type; }

    public ParkingTicket getTicket() { return ticket; }
    public void setTicket(ParkingTicket ticket) { this.ticket = ticket; }
}