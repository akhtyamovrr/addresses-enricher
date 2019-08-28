package org.test.enricher;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.test.enricher.model.Address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AroundEnricherTest {

    @Test
    public void testNoneResolved() {
        var buildings = Sets.newHashSet(
                new Address().id(1).latitude(0.0).longitude(0.0).city("city1").zipCode("00122").country("US").houseNumber("1"),
                new Address().id(2).latitude(5.0).longitude(10.0).city("city1").zipCode("00122").country("US").houseNumber("2"),
                new Address().id(3).latitude(10.0).longitude(0.0).city("city1").zipCode("00124").country("US").houseNumber("3"),
                new Address().id(4).latitude(5.0).longitude(5.0).country("US").houseNumber("4")
        );
        final var enricher = new AroundEnricher();
        enricher.enrichAddresses(buildings);
        assertEquals(1, buildings.stream().filter(building -> StringUtils.isEmpty(building.zipCode())).count());
        assertEquals(0, buildings.stream().filter(building -> StringUtils.isEmpty(building.city())).count());
    }

    @Test
    public void testResolved() {
        var buildings = Sets.newHashSet(
                new Address().id(1).latitude(0.0).longitude(0.0).city("city1").zipCode("00122").country("US").houseNumber("1"),
                new Address().id(2).latitude(5.0).longitude(10.0).city("city1").zipCode("00122").country("US").houseNumber("2"),
                new Address().id(3).latitude(10.0).longitude(0.0).city("city1").zipCode("00122").country("US").houseNumber("3"),
                new Address().id(4).latitude(5.0).longitude(5.0).country("US").houseNumber("4")
        );
        final var enricher = new AroundEnricher();
        enricher.enrichAddresses(buildings);
        assertTrue(buildings.stream().allMatch(building -> StringUtils.isNoneEmpty(building.zipCode())));
        assertTrue(buildings.stream().allMatch(building -> StringUtils.isNoneEmpty(building.city())));
    }
}
