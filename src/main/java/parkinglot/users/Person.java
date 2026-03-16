package parkinglot.users;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import parkinglot.models.Location;

@Embeddable
public record Person(
        String name,
        @Embedded Location address,
        String email,
        String phone
) {}