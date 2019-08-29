package org.test.enricher;

import org.apache.commons.lang3.StringUtils;
import org.test.enricher.datasource.TSVAddressesDataSource;
import org.test.enricher.model.Address;
import org.test.enricher.service.StreetNeighboursEnricherService;
import org.test.enricher.service.SurroundingEnricherService;
import org.test.enricher.writer.TSVAddressWriter;

import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("At least 1 argument required.\nArguments: <input_file_path> [output_file_path]\n" +
                    "If no output file is declared, result will be stored to 'result.tsv' at work directory");
            return;
        }

        final var start = System.currentTimeMillis();
        final var inputFile = args[0];
        final var outputFile = args.length >= 2 ? args[1] : "result.tsv";
        final var dataSource = new TSVAddressesDataSource(inputFile);
        final var writer = new TSVAddressWriter(outputFile);

        final var distinctStreetNames = dataSource.findDistinctStreetNames();
        for (String street: distinctStreetNames) {
            final var addresses = dataSource.findByStreetName(street);
            final var streetNeighboursEnricherService = new StreetNeighboursEnricherService();
            streetNeighboursEnricherService.enrichAddresses(addresses);
            final var aroundEnricherService = new SurroundingEnricherService();
            var unresolved = addresses.stream().filter(address -> StringUtils.isEmpty(address.zipCode())).collect(Collectors.toSet());
            for (Address address : unresolved) {
                final var area = dataSource.findSquareByCoordinates(address.latitude(), address.longitude(), 0.00045);
                aroundEnricherService.enrichAddresses(area);
            }
            writer.write(addresses);
        }
        final var end = System.currentTimeMillis();
        System.out.println("Processing took: " + (end - start));
    }
}
