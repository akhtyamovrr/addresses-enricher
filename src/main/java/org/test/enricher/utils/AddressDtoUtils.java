package org.test.enricher.utils;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.test.enricher.model.Address;
import org.test.enricher.model.dto.AddressDto;
import org.test.enricher.model.dto.AddressDtoComparator;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
public class AddressDtoUtils {
    private static Comparator<AddressDto> comparator = new AddressDtoComparator();

    public static Set<AddressDto> transformAddresses(Set<Address> addresses) {
        final Set<AddressDto> buildings = Sets.newHashSet();
        addresses.forEach(address -> {
            try {
                buildings.add(new AddressDto(address));
            } catch (Exception e) {
                log.error("failed to create building data entity", e);
            }
        });
        return buildings;
    }

    public static NavigableSet<AddressDto> oddAddresses(Set<AddressDto> buildings) {
        NavigableSet<AddressDto> oddSortedByHouseNumber = new TreeSet<>(comparator);
        oddSortedByHouseNumber.addAll(
                buildings.stream().filter(building -> building.houseNumber() % 2 == 1).collect(Collectors.toSet())
        );
        return oddSortedByHouseNumber;
    }

    public static NavigableSet<AddressDto> evenAddresses(Set<AddressDto> buildings) {
        NavigableSet<AddressDto> evenSortedByHouseNumber = new TreeSet<>(comparator);
        evenSortedByHouseNumber.addAll(
                buildings.stream().filter(building -> building.houseNumber() % 2 == 0).collect(Collectors.toSet())
        );
        return evenSortedByHouseNumber;
    }
}
