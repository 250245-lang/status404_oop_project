package parkinglot.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Account {

    public Admin(String userName, String password, Person person) {
        super(userName, password, person);
    }
    protected Admin() {}

    // Methods for adding floors, spots, etc. will be added as those classes are implemented.
}