package parkinglot.models.vehicles;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.VehicleType;

@Entity
@DiscriminatorValue("TRUCK")
public class Truck extends Vehicle {
    protected Truck(){super();}
    public Truck(String licenseNumber) {
        super(licenseNumber, VehicleType.TRUCK);
    }
}