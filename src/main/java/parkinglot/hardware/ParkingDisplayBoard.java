package parkinglot.hardware;

import parkinglot.models.spots.*;

public class ParkingDisplayBoard {
    private String id;
    private HandicappedSpot handicappedFreeSpot;
    private CompactSpot compactFreeSpot;
    private LargeSpot largeFreeSpot;
    private MotorbikeSpot motorbikeFreeSpot;
    private ElectricSpot electricFreeSpot;

    // Counts for display
    private int freeHandicapped;
    private int freeCompact;
    private int freeLarge;
    private int freeMotorbike;
    private int freeElectric;

    public ParkingDisplayBoard(String id) {
        this.id = id;
    }

    public void showEmptySpotNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Display Board [").append(id).append("] ===\n");
        sb.append(String.format("  Handicapped : %d free\n", freeHandicapped));
        sb.append(String.format("  Compact     : %d free\n", freeCompact));
        sb.append(String.format("  Large       : %d free\n", freeLarge));
        sb.append(String.format("  Motorbike   : %d free\n", freeMotorbike));
        sb.append(String.format("  Electric    : %d free\n", freeElectric));

        if (freeHandicapped + freeCompact + freeLarge + freeMotorbike + freeElectric == 0) {
            sb.append("  *** PARKING LOT IS FULL ***\n");
        }
        sb.append("================================");
        System.out.println(sb);
    }

    public void updateFreeCounts(int handicapped, int compact, int large, int motorbike, int electric) {
        this.freeHandicapped = handicapped;
        this.freeCompact = compact;
        this.freeLarge = large;
        this.freeMotorbike = motorbike;
        this.freeElectric = electric;
    }

    // Getters and Setters
    public String getId() { return id; }

    public int getFreeHandicapped() { return freeHandicapped; }
    public int getFreeCompact() { return freeCompact; }
    public int getFreeLarge() { return freeLarge; }
    public int getFreeMotorbike() { return freeMotorbike; }
    public int getFreeElectric() { return freeElectric; }

    public void setHandicappedFreeSpot(HandicappedSpot spot) { this.handicappedFreeSpot = spot; }
    public void setCompactFreeSpot(CompactSpot spot) { this.compactFreeSpot = spot; }
    public void setLargeFreeSpot(LargeSpot spot) { this.largeFreeSpot = spot; }
    public void setMotorbikeFreeSpot(MotorbikeSpot spot) { this.motorbikeFreeSpot = spot; }
    public void setElectricFreeSpot(ElectricSpot spot) { this.electricFreeSpot = spot; }
}