package parkinglot.models.vehicles;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.VehicleType;

@Entity
@DiscriminatorValue("VAN")
public class Van extends Vehicle {
    protected Van(){super();}
    public Van(String licenseNumber) {
        super(licenseNumber, VehicleType.VAN);
    }
}