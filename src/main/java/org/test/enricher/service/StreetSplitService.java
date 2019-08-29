package org.test.enricher.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.test.enricher.model.Address;
import org.test.enricher.model.dto.AddressDto;
import org.test.enricher.utils.AddressDtoUtils;

import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * this service gets all addresses at street with the same name and checks if neighbour buildings are located
 * at the same area by checking distance between them
 */
public class StreetSplitService {

    public Set<Set<Address>> split(Set<Address> addresses, double appropriateNeighbourDistanceDegrees) {
        final var addressDtoSet = AddressDtoUtils.transformAddresses(addresses);
        final var addressGroups = splitToGroups(AddressDtoUtils.oddAddresses(addressDtoSet), appropriateNeighbourDistanceDegrees);
        addressGroups.addAll(splitToGroups(AddressDtoUtils.evenAddresses(addressDtoSet), appropriateNeighbourDistanceDegrees));
        return addressGroups.stream().map(Sets::newHashSet).collect(Collectors.toSet());
    }

    private Set<LinkedList<Address>> splitToGroups(NavigableSet<AddressDto> sameStreetSideBuildings, double distance) {
        Set<LinkedList<Address>> addressGroups = Sets.newHashSet();
        for (AddressDto addressDto: sameStreetSideBuildings) {
            findGroupAndAdd(addressDto.address(), addressGroups, distance);
        }
        return addressGroups;
    }

    private void findGroupAndAdd(Address addressToCheck, Set<LinkedList<Address>> addressGroups, double distance) {
        for (LinkedList<Address> addressGroup: addressGroups) {
            if (isDistanceAppropriate(addressToCheck, addressGroup.getLast(), distance)) {
                addressGroup.addLast(addressToCheck);
                return;
            }
        }
        LinkedList<Address> newAddressGroup = Lists.newLinkedList();
        newAddressGroup.addLast(addressToCheck);
        addressGroups.add(newAddressGroup);
    }

    private boolean isDistanceAppropriate(Address from, Address to, double distance) {
        return Math.sqrt(square(from.latitude() - to.latitude()) + square(from.longitude() - to.longitude())) <= distance;
    }

    private double square(double value) {
        return value * value;
    }
}
