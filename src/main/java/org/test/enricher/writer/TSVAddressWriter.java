package org.test.enricher.writer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.test.enricher.model.Address;

import java.io.FileWriter;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@RequiredArgsConstructor
public class TSVAddressWriter implements AddressWriter {
    private final String filePath;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void write(Set<Address> addresses) throws Exception {
        try {
            lock.writeLock().lock();
            FileWriter out = new FileWriter(filePath, true);
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.TDF)) {
                for (Address address : addresses) {
                    printer.printRecord(address.id(), address.latitude(), address.longitude(), address.country(), address.state(), address.zipCode(), address.city(), address.street(), address.houseNumber());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
