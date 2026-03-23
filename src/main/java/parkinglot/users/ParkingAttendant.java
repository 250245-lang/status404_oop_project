package parkinglot.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ATTENDANT")
public class ParkingAttendant extends Account {

    public ParkingAttendant(String userName, String password, Person person) {
        super(userName, password, person);
    }
    protected ParkingAttendant() {}
}