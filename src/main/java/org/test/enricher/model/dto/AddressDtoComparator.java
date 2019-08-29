package org.test.enricher.model.dto;

import java.util.Comparator;

public class AddressDtoComparator implements Comparator<AddressDto> {

    @Override
    public int compare(AddressDto o1, AddressDto o2) {
        return Integer.compare(o1.houseNumber(), o2.houseNumber());
    }
}
