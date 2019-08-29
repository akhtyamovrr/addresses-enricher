package org.test.enricher.internal;

import org.test.enricher.datasource.TSVAddressesDataSource;
import org.test.enricher.model.Address;

import java.util.Map;
import java.util.stream.Collectors;

// for internal usage. Shows how many same zips in file in format {zip_code=count}
public class StatsByZipCodes {
    public static void main(String[] args) throws Exception {
        final var tsvAddressesDataSource = new TSVAddressesDataSource("result.tsv");
        Map<String, Long> counts =
                tsvAddressesDataSource.findAll().stream().map(Address::zipCode).collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        System.out.println(counts);
    }
}