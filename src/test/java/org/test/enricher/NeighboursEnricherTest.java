package org.test.enricher;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.test.enricher.model.Address;
import org.test.enricher.model.AddressByHouseNumberComparator;
import org.test.enricher.model.BuildingData;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeighboursEnricherTest {
    @Test
    public void testCompeteStreetWithSameZipCode() {
        final var buildings = Sets.newHashSet(
                new BuildingData(new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1")),
                new BuildingData(new Address().id(2).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("2")),
                new BuildingData(new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3")),
                new BuildingData(new Address().id(4).latitude(52.05).longitude(30.2).country("US").houseNumber("4")),
                new BuildingData(new Address().id(5).latitude(52.05).longitude(30.2).country("US").houseNumber("5")),
                new BuildingData(new Address().id(6).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("6")),
                new BuildingData(new Address().id(7).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("7"))
        );
        new NeighboursEnricher(new AddressByHouseNumberComparator(), buildings).enrichAddresses();
        assertTrue(buildings.stream().allMatch(buildingData -> "city1".equals(buildingData.address().city()) && "00122".equals(buildingData.address().zipCode())));
    }

    @Test
    public void testTwoIntervalsWithTwoUnknown() {
        final var buildings = Sets.newHashSet(
                new BuildingData(new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1")),
                new BuildingData(new Address().id(2).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("2")),
                new BuildingData(new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3")),
                new BuildingData(new Address().id(4).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("4")),
                new BuildingData(new Address().id(5).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("5")),
                new BuildingData(new Address().id(6).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("6")),
                new BuildingData(new Address().id(7).latitude(52.05).longitude(30.2).country("US").houseNumber("7")),
                new BuildingData(new Address().id(8).latitude(52.05).longitude(30.2).country("US").houseNumber("8")),
                new BuildingData(new Address().id(9).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("9")),
                new BuildingData(new Address().id(10).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("10")),
                new BuildingData(new Address().id(11).latitude(52.05).longitude(30.2).country("US").houseNumber("11")),
                new BuildingData(new Address().id(13).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("13"))
        );
        resolveAndCheckUnknownIds(buildings, Sets.newHashSet(7L, 8L));
    }

    @Test
    public void testFirstBuildingUnknown() {
        final var buildings = Sets.newHashSet(
                new BuildingData(new Address().id(1).latitude(52.1).longitude(30.2).country("US").houseNumber("1")),
                new BuildingData(new Address().id(3).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("3")),
                new BuildingData(new Address().id(5).latitude(52.05).longitude(30.2).country("US").houseNumber("5")),
                new BuildingData(new Address().id(6).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("6")),
                new BuildingData(new Address().id(7).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("7"))
        );
        resolveAndCheckUnknownIds(buildings, Collections.singleton(1L));
    }

    @Test
    public void testLastBuildingsUnknown() {
        final var buildings = Sets.newHashSet(
                new BuildingData(new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1")),
                new BuildingData(new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3")),
                new BuildingData(new Address().id(5).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("5")),
                new BuildingData(new Address().id(7).latitude(52.05).longitude(30.2).country("US").houseNumber("7")),
                new BuildingData(new Address().id(9).latitude(52.05).longitude(30.2).country("US").houseNumber("9"))
        );
        resolveAndCheckUnknownIds(buildings, Sets.newHashSet(7L, 9L));
    }

    @Test
    public void testWithKnownWithoutInterval() {
        final var buildings = Sets.newHashSet(
                new BuildingData(new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1")),
                new BuildingData(new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3")),
                new BuildingData(new Address().id(5).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("5")),
                new BuildingData(new Address().id(7).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("7")),
                new BuildingData(new Address().id(9).latitude(52.05).longitude(30.2).country("US").houseNumber("9")),
                new BuildingData(new Address().id(11).latitude(52.05).longitude(30.2).country("US").houseNumber("11")),
                new BuildingData(new Address().id(13).latitude(52.05).longitude(30.2).city("city2").zipCode("00126").country("US").houseNumber("13"))
        );
        resolveAndCheckUnknownIds(buildings, Sets.newHashSet(9L, 11L));
    }

    @Test
    public void testUnknownAfterEachKnown() {
        final var buildings = Sets.newHashSet(
                new BuildingData(new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1")),
                new BuildingData(new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3")),
                new BuildingData(new Address().id(5).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("5")),
                new BuildingData(new Address().id(7).latitude(52.05).longitude(30.2).country("US").houseNumber("7")),
                new BuildingData(new Address().id(9).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("9")),
                new BuildingData(new Address().id(11).latitude(52.05).longitude(30.2).country("US").houseNumber("11")),
                new BuildingData(new Address().id(13).latitude(52.05).longitude(30.2).city("city2").zipCode("00126").country("US").houseNumber("13")),
                new BuildingData(new Address().id(15).latitude(52.05).longitude(30.2).country("US").houseNumber("15"))
        );
        resolveAndCheckUnknownIds(buildings, Sets.newHashSet(7L, 11L, 15L));
    }

    private void resolveAndCheckUnknownIds(Set<BuildingData> buildings, Set<Long> ids) {
        new NeighboursEnricher(new AddressByHouseNumberComparator(), buildings).enrichAddresses();
        final var unresolved = buildings
                .stream()
                .filter(buildingData -> StringUtils.isEmpty(buildingData.address().zipCode()))
                .collect(Collectors.toSet());
        assertEquals(ids.size(), unresolved.size());
        assertTrue(unresolved.stream().map(buildingData -> buildingData.address().id()).collect(Collectors.toSet()).containsAll(ids));
    }
}
