package org.test.enricher.datasource;

import org.test.enricher.model.Address;

import java.util.Set;

public interface AddressesDataSource {
    Set<String> findDistinctStreetNames() throws Exception;
    Set<Address> findByStreetName(String streetName) throws Exception;
    Set<Address> findSquareByCoordinates(double latitute, double longtitide, double distance) throws Exception;
}
