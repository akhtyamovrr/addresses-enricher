package org.test.enricher.writer;

import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.test.enricher.model.Address;

import java.io.FileWriter;
import java.util.Set;

@AllArgsConstructor
public class TSVAddressWriter implements AddressWriter {
    private String filePath;

    @Override
    public void write(Set<Address> addresses) throws Exception {
        FileWriter out = new FileWriter(filePath, true);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.TDF)) {
            for (Address address : addresses) {
                printer.printRecord(address.id(), address.latitude(), address.longitude(), address.country(), address.state(), address.zipCode(), address.city(), address.street(), address.houseNumber());
            }
        }
    }
}
