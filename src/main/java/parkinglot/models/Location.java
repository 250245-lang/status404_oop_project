package parkinglot.models;

import jakarta.persistence.Embeddable;

@Embeddable
public record Location(String streetAddress, String city, String state, String zipcode, String country){}
