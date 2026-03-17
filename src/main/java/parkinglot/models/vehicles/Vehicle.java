package parkinglot.models.vehicles;

import parkinglot.constants.VehicleType;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_category")
public abstract class Vehicle {
    @Id
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    protected Vehicle() {}

    public Vehicle(String licenseNumber, VehicleType type) {
        this.licenseNumber = licenseNumber;
        this.type = type;
    }

    // Getters and Setters
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
}
