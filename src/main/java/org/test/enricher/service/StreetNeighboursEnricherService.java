package org.test.enricher.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.test.enricher.model.Address;
import org.test.enricher.model.dto.AddressDto;
import org.test.enricher.utils.AddressDtoUtils;

import java.util.NavigableSet;
import java.util.Set;

@Slf4j
public class StreetNeighboursEnricherService implements Enricher {

    public void enrichAddresses(Set<Address> addresses) {
        final Set<AddressDto> buildings = AddressDtoUtils.transformAddresses(addresses);
        NavigableSet<AddressDto> oddSortedByHouseNumber = AddressDtoUtils.oddAddresses(buildings);
        NavigableSet<AddressDto> evenSortedByHouseNumber = AddressDtoUtils.evenAddresses(buildings);
        enrichAddresses(oddSortedByHouseNumber);
        enrichAddresses(evenSortedByHouseNumber);
    }

    private void enrichAddresses(NavigableSet<AddressDto> buildings) {
        if (CollectionUtils.isEmpty(buildings)) {
            return;
        }
        AddressDto leftKnownData;
        leftKnownData = getFirstKnownBuilding(buildings);
        if (leftKnownData == null) {
            return;
        }

        ZipCodeInterval zipCodeInterval;
        do {
            zipCodeInterval = findZipCodeInterval(leftKnownData, buildings);
            enrichBuildingsBetween(zipCodeInterval.from, zipCodeInterval.to, buildings);
            leftKnownData = zipCodeInterval.getNextKnownBuilding();
        } while (leftKnownData != null);
    }

    private AddressDto getFirstKnownBuilding(NavigableSet<AddressDto> buildings) {
        AddressDto firstKnownBuilding;
        var currentBuilding = buildings.first();
        if (StringUtils.isNoneEmpty(currentBuilding.address().zipCode())) {
            firstKnownBuilding = currentBuilding;
        } else {
            firstKnownBuilding = findHigherKnown(currentBuilding, buildings);
        }
        return firstKnownBuilding;
    }

    private ZipCodeInterval findZipCodeInterval(AddressDto from, NavigableSet<AddressDto> buildings) {
        AddressDto currentBuilding = from;
        AddressDto to;
        do {
            to = currentBuilding;
            currentBuilding = findHigherKnown(currentBuilding, buildings);
        } while (currentBuilding != null && currentBuilding.address().zipCode().equals(from.address().zipCode()));
        return new ZipCodeInterval(from, to, currentBuilding);
    }

    private void enrichBuildingsBetween(AddressDto from, AddressDto to, NavigableSet<AddressDto> buildings) {
        if (from != to) {
            AddressDto currentBuilding = buildings.higher(from);
            final var zipCode = from.address().zipCode();
            String city = null;
            boolean isSameCity = false;
            if (StringUtils.equalsIgnoreCase(from.address().city(), to.address().city())) {
                city = from.address().city();
                isSameCity = true;
            }
            while (currentBuilding != null && currentBuilding != to) {
                if (StringUtils.isEmpty(currentBuilding.address().zipCode())) {
                    currentBuilding.address().zipCode(zipCode);
                    log.info("enriched zip for: {}", currentBuilding.address().id());
                }
                if (isSameCity && StringUtils.isEmpty(currentBuilding.address().city())) {
                    currentBuilding.address().city(city);
                    log.info("enriched city for: {}", currentBuilding.address().id());
                }
                currentBuilding = buildings.higher(currentBuilding);
            }
        }
    }

    private AddressDto findHigherKnown(AddressDto building, NavigableSet<AddressDto> buildings) {
        if (building == null) {
            return null;
        }
        AddressDto currentBuilding = buildings.higher(building);
        while (currentBuilding != null && StringUtils.isEmpty(currentBuilding.address().zipCode())) {
            currentBuilding = buildings.higher(currentBuilding);
        }
        return currentBuilding;
    }

    @AllArgsConstructor
    @Getter
    private class ZipCodeInterval {
        private AddressDto from;
        private AddressDto to;
        private AddressDto nextKnownBuilding;
    }
}
