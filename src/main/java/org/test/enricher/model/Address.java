package org.test.enricher.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
public class Address {
    private long id;
    private double latitude;
    private double longitude;
    private String country;
    private String state;
    private String zipCode;
    private String city;
    private String street;
    private String houseNumber;
}
