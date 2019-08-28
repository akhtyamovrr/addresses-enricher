package org.test.enricher.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.test.enricher.model.Address;
import org.test.enricher.model.AddressByHouseNumberComparator;
import org.test.enricher.model.BuildingData;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
public class StreetNeighboursEnricherService implements Enricher {
    private Comparator<BuildingData> comparator = new AddressByHouseNumberComparator();

    public StreetNeighboursEnricherService() {

    }

    public StreetNeighboursEnricherService(Comparator<BuildingData> comparator) {
        this.comparator = comparator;
    }

    public void enrichAddresses(Set<Address> addresses) {
        final var buildings = addresses.stream().map(BuildingData::new).collect(Collectors.toSet());
        NavigableSet<BuildingData> oddSortedByHouseNumber = new TreeSet<>(comparator);
        oddSortedByHouseNumber.addAll(buildings.stream().filter(building -> building.houseNumber() % 2 == 1).collect(Collectors.toSet()));
        NavigableSet<BuildingData> evenSortedByHouseNumber = new TreeSet<>(comparator);
        evenSortedByHouseNumber.addAll(buildings.stream().filter(building -> building.houseNumber() % 2 == 0).collect(Collectors.toSet()));
        enrichAddresses(oddSortedByHouseNumber);
        enrichAddresses(evenSortedByHouseNumber);
    }

    private void enrichAddresses(NavigableSet<BuildingData> buildings) {
        if (CollectionUtils.isEmpty(buildings)) {
            return;
        }
        BuildingData leftKnownData;
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

    private BuildingData getFirstKnownBuilding(NavigableSet<BuildingData> buildings) {
        BuildingData firstKnownBuilding;
        var currentBuilding = buildings.first();
        if (StringUtils.isNoneEmpty(currentBuilding.address().zipCode())) {
            firstKnownBuilding = currentBuilding;
        } else {
            firstKnownBuilding = findHigherKnown(currentBuilding, buildings);
        }
        return firstKnownBuilding;
    }

    private ZipCodeInterval findZipCodeInterval(BuildingData from, NavigableSet<BuildingData> buildings) {
        BuildingData currentBuilding = from;
        BuildingData to;
        do {
            to = currentBuilding;
            currentBuilding = findHigherKnown(currentBuilding, buildings);
        } while (currentBuilding != null && currentBuilding.address().zipCode().equals(from.address().zipCode()));
        return new ZipCodeInterval(from, to, currentBuilding);
    }

    private void enrichBuildingsBetween(BuildingData from, BuildingData to, NavigableSet<BuildingData> buildings) {
        if (from != to) {
            BuildingData currentBuilding = buildings.higher(from);
            final var zipCode = from.address().zipCode();
            String city = null;
            boolean isSameCity = false;
            if (StringUtils.equalsIgnoreCase(from.address().city(), to.address().city())) {
                city = from.address().city();
                isSameCity = true;
            }
            while (currentBuilding != null && currentBuilding != to) {
                currentBuilding.address().zipCode(zipCode);
                log.info("enriched zip for: {}", currentBuilding.address().id());
                if (isSameCity) {
                    currentBuilding.address().city(city);
                    log.info("enriched city for: {}", currentBuilding.address().id());
                }
                currentBuilding = buildings.higher(currentBuilding);
            }
        }
    }

    private BuildingData findHigherKnown(BuildingData building, NavigableSet<BuildingData> buildings) {
        if (building == null) {
            return null;
        }
        BuildingData currentBuilding = buildings.higher(building);
        while (currentBuilding != null && StringUtils.isEmpty(currentBuilding.address().zipCode())) {
            currentBuilding = buildings.higher(currentBuilding);
        }
        return currentBuilding;
    }

    @AllArgsConstructor
    @Getter
    private class ZipCodeInterval {
        private BuildingData from;
        private BuildingData to;
        private BuildingData nextKnownBuilding;
    }
}
