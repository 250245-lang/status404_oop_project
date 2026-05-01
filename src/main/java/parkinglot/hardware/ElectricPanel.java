package parkinglot.hardware;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class ElectricPanel {
    @Id
    private String id; // This will match the spot number
    private int totalChargedMinutes;
    private LocalDateTime chargingStartTime;
    private boolean charging;

    protected ElectricPanel() {}

    public ElectricPanel(String id) {
        this.id = id;
        this.charging = false;
        this.totalChargedMinutes = 0;
    }

    public void startCharging() {
        if (!charging) {
            this.charging = true;
            this.chargingStartTime = LocalDateTime.now();
        }
    }

    public void stopCharging() {
        if (charging && chargingStartTime != null) {
            long minutes = ChronoUnit.MINUTES.between(chargingStartTime, LocalDateTime.now());
            this.totalChargedMinutes += (int) minutes;
            this.charging = false;
            this.chargingStartTime = null;
        }
    }

    public int getCurrentlyChargingMinutes() {
        if (charging && chargingStartTime != null) {
            return (int) ChronoUnit.MINUTES.between(chargingStartTime, LocalDateTime.now());
        }
        return 0;
    }

    // Getters and Setters
    public int getTotalChargedMinutes() { return totalChargedMinutes; }
    public void setTotalChargedMinutes(int m) { this.totalChargedMinutes = m; }

    public LocalDateTime getChargingStartTime() { return chargingStartTime; }
    public boolean isCharging() { return charging; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}