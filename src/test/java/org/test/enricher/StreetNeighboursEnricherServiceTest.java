package org.test.enricher;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.test.enricher.model.Address;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreetNeighboursEnricherServiceTest {

    private StreetNeighboursEnricherService neighboursEnricher = new StreetNeighboursEnricherService();

    @Test
    public void testCompeteStreetWithSameZipCode() {
        final var buildings = Sets.newHashSet(
                new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1"),
                new Address().id(2).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("2"),
                new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3"),
                new Address().id(4).latitude(52.05).longitude(30.2).country("US").houseNumber("4"),
                new Address().id(5).latitude(52.05).longitude(30.2).country("US").houseNumber("5"),
                new Address().id(6).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("6"),
                new Address().id(7).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("7")
        );
        neighboursEnricher.enrichAddresses(buildings);
        assertTrue(buildings.stream().allMatch(buildingData -> "city1".equals(buildingData.city()) && "00122".equals(buildingData.zipCode())));
    }

    @Test
    public void testTwoIntervalsWithTwoUnknown() {
        final var buildings = Sets.newHashSet(
                new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1"),
                new Address().id(2).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("2"),
                new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3"),
                new Address().id(4).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("4"),
                new Address().id(5).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("5"),
                new Address().id(6).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("6"),
                new Address().id(7).latitude(52.05).longitude(30.2).country("US").houseNumber("7"),
                new Address().id(8).latitude(52.05).longitude(30.2).country("US").houseNumber("8"),
                new Address().id(9).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("9"),
                new Address().id(10).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("10"),
                new Address().id(11).latitude(52.05).longitude(30.2).country("US").houseNumber("11"),
                new Address().id(13).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("13")
        );
        resolveAndCheckUnknownIds(buildings, Sets.newHashSet(7L, 8L));
    }

    @Test
    public void testFirstBuildingUnknown() {
        final var buildings = Sets.newHashSet(
                new Address().id(1).latitude(52.1).longitude(30.2).country("US").houseNumber("1"),
                new Address().id(3).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("3"),
                new Address().id(5).latitude(52.05).longitude(30.2).country("US").houseNumber("5"),
                new Address().id(6).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("6"),
                new Address().id(7).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("7")
        );
        resolveAndCheckUnknownIds(buildings, Collections.singleton(1L));
    }

    @Test
    public void testLastBuildingsUnknown() {
        final var buildings = Sets.newHashSet(
                new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1"),
                new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3"),
                new Address().id(5).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("5"),
                new Address().id(7).latitude(52.05).longitude(30.2).country("US").houseNumber("7"),
                new Address().id(9).latitude(52.05).longitude(30.2).country("US").houseNumber("9")
        );
        resolveAndCheckUnknownIds(buildings, Sets.newHashSet(7L, 9L));
    }

    @Test
    public void testWithKnownWithoutInterval() {
        final var buildings = Sets.newHashSet(
                new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1"),
                new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3"),
                new Address().id(5).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("5"),
                new Address().id(7).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("7"),
                new Address().id(9).latitude(52.05).longitude(30.2).country("US").houseNumber("9"),
                new Address().id(11).latitude(52.05).longitude(30.2).country("US").houseNumber("11"),
                new Address().id(13).latitude(52.05).longitude(30.2).city("city2").zipCode("00126").country("US").houseNumber("13")
        );
        resolveAndCheckUnknownIds(buildings, Sets.newHashSet(9L, 11L));
    }

    @Test
    public void testUnknownAfterEachKnown() {
        final var buildings = Sets.newHashSet(
                new Address().id(1).latitude(52.1).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("1"),
                new Address().id(3).latitude(52.05).longitude(30.2).country("US").houseNumber("3"),
                new Address().id(5).latitude(52.05).longitude(30.2).city("city1").zipCode("00122").country("US").houseNumber("5"),
                new Address().id(7).latitude(52.05).longitude(30.2).country("US").houseNumber("7"),
                new Address().id(9).latitude(52.05).longitude(30.2).city("city1").zipCode("00124").country("US").houseNumber("9"),
                new Address().id(11).latitude(52.05).longitude(30.2).country("US").houseNumber("11"),
                new Address().id(13).latitude(52.05).longitude(30.2).city("city2").zipCode("00126").country("US").houseNumber("13"),
                new Address().id(15).latitude(52.05).longitude(30.2).country("US").houseNumber("15")
        );
        resolveAndCheckUnknownIds(buildings, Sets.newHashSet(7L, 11L, 15L));
    }

    private void resolveAndCheckUnknownIds(Set<Address> buildings, Set<Long> ids) {
        neighboursEnricher.enrichAddresses(buildings);
        final var unresolved = buildings
                .stream()
                .filter(buildingData -> StringUtils.isEmpty(buildingData.zipCode()))
                .collect(Collectors.toSet());
        assertEquals(ids.size(), unresolved.size());
        assertTrue(unresolved.stream().map(Address::id).collect(Collectors.toSet()).containsAll(ids));
    }
}
