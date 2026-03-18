package parkinglot.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ParkingRate {

    @Id
    private String id = "DEFAULT_RATE";

    private double firstHourRate = 4.0;
    private double secondHourRate = 3.5;
    private double thirdHourRate = 3.5;
    private double remainingHourRate = 2.5;

    public ParkingRate() {}

    public double calculateFee(long durationMinutes) {
        if (durationMinutes <= 0) return 0.0;

        double hours = Math.ceil(durationMinutes / 60.0);
        double totalFee = 0;

        for (int i = 1; i <= hours; i++) {
            if (i == 1) {
                totalFee += firstHourRate;
            } else if (i == 2) {
                totalFee += secondHourRate;
            } else if (i == 3) {
                totalFee += thirdHourRate;
            } else {
                totalFee += remainingHourRate;
            }
        }

        return totalFee;
    }

    public double getFirstHourRate() { return firstHourRate; }
    public void setFirstHourRate(double firstHourRate) { this.firstHourRate = firstHourRate; }
    public double getSecondHourRate() { return secondHourRate; }
    public void setSecondHourRate(double secondHourRate) { this.secondHourRate = secondHourRate; }
    public double getThirdHourRate() { return thirdHourRate; }
    public void setThirdHourRate(double thirdHourRate) { this.thirdHourRate = thirdHourRate; }
    public double getRemainingHourRate() { return remainingHourRate; }
    public void setRemainingHourRate(double remainingHourRate) { this.remainingHourRate = remainingHourRate; }
}
