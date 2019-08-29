package org.test.enricher.executor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.test.enricher.datasource.AddressesDataSource;
import org.test.enricher.model.Address;
import org.test.enricher.service.StreetNeighboursEnricherService;
import org.test.enricher.service.SurroundingEnricherService;
import org.test.enricher.writer.AddressWriter;

import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class AddressesEnricherExecutor {
    private final AddressesDataSource dataSource;
    private final AddressWriter writer;
    private final double searchAroundMeters;

    // One degree is about 111km
    private static final double METERS_IN_DEGREE = 111_000;

    public void execute() {
        try {
            final var distinctStreetNames = dataSource.findDistinctStreetNames();
            distinctStreetNames.parallelStream().forEach(street -> {
                try {
                    final var addresses = dataSource.findByStreetName(street);
                    final var streetNeighboursEnricherService = new StreetNeighboursEnricherService();
                    streetNeighboursEnricherService.enrichAddresses(addresses);
                    final var aroundEnricherService = new SurroundingEnricherService();
                    var unresolved = addresses.stream().filter(address -> StringUtils.isEmpty(address.zipCode())).collect(Collectors.toSet());
                    for (Address address : unresolved) {
                        final var area = dataSource.findSquareByCoordinates(address.latitude(), address.longitude(), searchAroundMeters / METERS_IN_DEGREE);
                        aroundEnricherService.enrichAddresses(area);
                    }
                    writer.write(addresses);
                } catch (Exception e) {
                    log.error("failed to process {}", street, e);
                }
            });
        } catch (Throwable t) {
            log.error("failed to process data", t);
        }
    }
}
