package org.test.enricher.writer;

import org.test.enricher.model.Address;

import java.util.Set;

public interface AddressWriter {
    void write(Set<Address> addresses) throws Exception;
}
