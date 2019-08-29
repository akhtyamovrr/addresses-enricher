package org.test.enricher.service;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.test.enricher.model.Address;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreetSplitTest {
    private StreetSplitService service = new StreetSplitService();

    @Test
    public void test() {
        final var addresses = Sets.newHashSet(
                new Address().latitude(37.35514939144903).longitude(-122.02948794610286).houseNumber("1"),
                new Address().latitude(37.35718543332277).longitude(-122.02968955163723).houseNumber("3"),
                new Address().latitude(37.35518543332277).longitude(-122.02968955163723).houseNumber("5"),
                new Address().latitude(37.35514939144903).longitude(-122.02948794610286).houseNumber("2"),
                new Address().latitude(37.35518543332277).longitude(-122.02968955163723).houseNumber("4")
        );
        final var split = service.split(addresses, 200d / 111_000);
        assertEquals(3, split.size());
    }
}
