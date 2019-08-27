package org.test.enricher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.test.enricher.model.BuildingData;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class NeighboursEnricher {
    private final NavigableSet<BuildingData> oddSortedByHouseNumber;
    private final NavigableSet<BuildingData> evenSortedByHouseNumber;

    public NeighboursEnricher(Comparator<BuildingData> comparator, Set<BuildingData> housesData) {
        oddSortedByHouseNumber = new TreeSet<>(comparator);
        oddSortedByHouseNumber.addAll(housesData.stream().filter(house -> house.houseNumber() % 2 == 1).collect(Collectors.toSet()));
        evenSortedByHouseNumber = new TreeSet<>(comparator);
        evenSortedByHouseNumber.addAll(housesData.stream().filter(house -> house.houseNumber() % 2 == 0).collect(Collectors.toSet()));
    }

    public void enrichAddresses() {
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
            while (currentBuilding != null && currentBuilding != to) {
                currentBuilding.address().zipCode(from.address().zipCode());
                currentBuilding.address().city(from.address().city());
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
