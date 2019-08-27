package org.test.enricher.model;

import java.util.Comparator;

public class AddressByHouseNumberComparator implements Comparator<BuildingData> {

    @Override
    public int compare(BuildingData o1, BuildingData o2) {
        return Integer.compare(o1.houseNumber(), o2.houseNumber());
    }
}
