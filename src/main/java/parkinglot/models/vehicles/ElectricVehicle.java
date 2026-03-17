package parkinglot.models.vehicles;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.VehicleType;

@Entity
@DiscriminatorValue("ELECTRIC")
public class ElectricVehicle extends Vehicle {
    protected ElectricVehicle(){super();}
    public ElectricVehicle(String licenseNumber) {
        super(licenseNumber, VehicleType.ELECTRIC);
    }
}