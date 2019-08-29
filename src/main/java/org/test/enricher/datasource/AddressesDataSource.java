package org.test.enricher.datasource;

import org.test.enricher.model.Address;

import java.util.Set;

public interface AddressesDataSource {
    Set<Address> findAll() throws Exception;
    Set<String> findDistinctStreetNames() throws Exception;
    Set<Address> findByStreetName(String streetName) throws Exception;

    /**
     * Finds all addresses with coordinates inside square (latitude - distance, longitude - distance),
     * (latitude - distance, longitude + distance), (latitude + distance, longitude + distance),
     * (latitude + distance, longitude - distance)
     * @param latitude - lat of area center
     * @param longitude - lon of area center
     * @param distance - how many degrees in each direction should be searched
     * @return addresses that match search area
     * @throws Exception
     */
    Set<Address> findSquareByCoordinates(double latitude, double longitude, double distance) throws Exception;
}
