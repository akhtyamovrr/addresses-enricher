package org.test.enricher.service;

import org.test.enricher.model.Address;

import java.util.Set;

public interface Enricher {
    void enrichAddresses(Set<Address> addresses);
}
