package org.test.enricher;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.test.enricher.model.Address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AroundEnricherTest {

    private AroundEnricherService enricher = new AroundEnricherService();

    @Test
    public void testNoneResolved() {
        var buildings = Sets.newHashSet(
                new Address().id(1).latitude(0.0).longitude(0.0).city("city1").zipCode("00122").country("US").houseNumber("1"),
                new Address().id(2).latitude(5.0).longitude(10.0).city("city1").zipCode("00122").country("US").houseNumber("2"),
                new Address().id(3).latitude(10.0).longitude(0.0).city("city1").zipCode("00124").country("US").houseNumber("3"),
                new Address().id(4).latitude(5.0).longitude(5.0).country("US").houseNumber("4")
        );
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
        enricher.enrichAddresses(buildings);
        assertTrue(buildings.stream().allMatch(building -> StringUtils.isNoneEmpty(building.zipCode())));
        assertTrue(buildings.stream().allMatch(building -> StringUtils.isNoneEmpty(building.city())));
    }

    @Test
    public void testPartiallyResolved() {
        var buildings = Sets.newHashSet(
                new Address().id(1).latitude(0.0).longitude(0.0).city("city1").zipCode("00124").country("US").houseNumber("1"),
                new Address().id(2).latitude(5.0).longitude(10.0).city("city1").zipCode("00124").country("US").houseNumber("2"),
                new Address().id(5).latitude(10.0).longitude(10.0).city("city2").zipCode("00122").country("US").houseNumber("5"),
                new Address().id(6).latitude(20.0).longitude(10.0).city("city2").zipCode("00122").country("US").houseNumber("6"),
                new Address().id(9).latitude(20.0).longitude(0.0).city("city2").zipCode("00122").country("US").houseNumber("9"),
                new Address().id(10).latitude(10.0).longitude(0.5).city("city2").zipCode("00122").country("US").houseNumber("10"),
                new Address().id(11).latitude(30.0).longitude(5.0).city("city3").zipCode("00122").country("US").houseNumber("11"),
                new Address().id(3).latitude(10.0).longitude(0.0).country("US").houseNumber("3"),
                new Address().id(4).latitude(5.0).longitude(5.0).country("US").houseNumber("4"),
                new Address().id(7).latitude(18.0).longitude(2.0).city("city2").country("US").houseNumber("7"),
                new Address().id(8).latitude(15.0).longitude(1.0).zipCode("00122").country("US").houseNumber("8"),
                new Address().id(12).latitude(15.0).longitude(1.0).country("US").houseNumber("12")

        );
        enricher.enrichAddresses(buildings);
        assertEquals(7, buildings.stream().filter(data -> "city2".equalsIgnoreCase(data.city())).count());
        assertEquals(8, buildings.stream().filter(data -> "00122".equalsIgnoreCase(data.zipCode())).count());
    }
}
