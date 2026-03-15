package parkinglot.users;

import parkinglot.models.Location;

public record Person(
        String name,
        Location address,
        String email,
        String phone
) {}