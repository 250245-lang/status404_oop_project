package parkinglot.models.vehicles;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.VehicleType;

@Entity
@DiscriminatorValue("MOTORBIKE")
public class Motorbike extends Vehicle {
    protected Motorbike(){super();}
    public Motorbike(String licenseNumber) {
        super(licenseNumber, VehicleType.MOTORBIKE);
    }
}