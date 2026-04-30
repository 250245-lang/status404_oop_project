package parkinglot.models;

import parkinglot.constants.ParkingTicketStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "parking_tickets")
public class ParkingTicket {

    @Id
    private String ticketNumber;

    private LocalDateTime issuedAt;
    private LocalDateTime payedAt;
    private double payedAmount;

    @Enumerated(EnumType.STRING)
    private ParkingTicketStatus status;

    private String spotNumber;
    private String vehicleLicense;
    private int chargingMinutes; // New: track charging duration

    public ParkingTicket() {
        this.ticketNumber = generateTimeBasedId();
        this.issuedAt = LocalDateTime.now();
        this.status = ParkingTicketStatus.ACTIVE;
        this.payedAmount = 0.0;
    }

    public ParkingTicket(String vehicleLicense, String spotNumber) {
        this();
        this.vehicleLicense = vehicleLicense;
        this.spotNumber = spotNumber;
    }

    private String generateTimeBasedId() {
        LocalDateTime epoch = LocalDateTime.of(2026, 5, 1, 0, 0, 0);
        LocalDateTime now = LocalDateTime.now();

        double millisPassed = ChronoUnit.MILLIS.between(epoch, now);

        int n = 3;

        long increment = (long) (n * millisPassed / 1000);

        if (increment >= Math.pow(36, 4)) {
            return Long.toString(increment, 36).toUpperCase();
        }

        return String.format("%4s", Long.toString(increment, 36).toUpperCase())
                .replace(' ', '0');
    }

    public long getParkingDurationMinutes() {
        LocalDateTime end = (payedAt != null) ? payedAt : LocalDateTime.now();
        return ChronoUnit.MINUTES.between(issuedAt, end);
    }

    public boolean markPaid(double amount) {
        if (status == ParkingTicketStatus.PAID) {
            System.out.println("Ticket " + ticketNumber + " is already paid.");
            return false;
        }
        this.payedAmount = amount;
        this.payedAt = LocalDateTime.now();
        this.status = ParkingTicketStatus.PAID;
        return true;
    }

    public boolean markLost() {
        this.status = ParkingTicketStatus.LOST;
        return true;
    }

    @Transient // This tells JPA not to save this as a column (it's a helper method)
    public boolean isPaid() {
        return status == ParkingTicketStatus.PAID;
    }

    // Getters and Setters
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getPayedAt() { return payedAt; }
    public void setPayedAt(LocalDateTime payedAt) { this.payedAt = payedAt; }

    public double getPayedAmount() { return payedAmount; }
    public void setPayedAmount(double payedAmount) { this.payedAmount = payedAmount; }

    public ParkingTicketStatus getStatus() { return status; }
    public void setStatus(ParkingTicketStatus status) { this.status = status; }

    public String getSpotNumber() { return spotNumber; }
    public void setSpotNumber(String spotNumber) { this.spotNumber = spotNumber; }

    public String getVehicleLicense() { return vehicleLicense; }
    public void setVehicleLicense(String vehicleLicense) { this.vehicleLicense = vehicleLicense; }

    public int getChargingMinutes() { return chargingMinutes; }
    public void setChargingMinutes(int chargingMinutes) { this.chargingMinutes = chargingMinutes; }

}