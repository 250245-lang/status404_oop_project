package parkinglot.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ParkingRate {

    @Id
    private String id = "DEFAULT_RATE";

    private double hourlyRate = 3.5;

    public ParkingRate() {}

    public double calculateFee(long durationMinutes) {
        if (durationMinutes <= 0) return 0.0;
        double hours = Math.ceil(durationMinutes / 60.0);
        return hours * hourlyRate;
    }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
}
