package parkinglot.models.vehicles;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import parkinglot.constants.VehicleType;

@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {
    protected Car(){super();}
    public Car(String licenseNumber) {
        super(licenseNumber, VehicleType.CAR);
    }
}