package org.test.enricher.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.test.enricher.datasource.AddressesDataSource;
import org.test.enricher.model.Address;
import org.test.enricher.service.StreetNeighboursEnricherService;
import org.test.enricher.service.StreetSplitService;
import org.test.enricher.service.SurroundingEnricherService;
import org.test.enricher.writer.AddressWriter;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class AddressesEnricherExecutor {
    private final AddressesDataSource dataSource;
    private final AddressWriter writer;
    private final double searchAroundMeters;
    private final double maxDistanceToSameStreetNeighboursMeters;
    private StreetNeighboursEnricherService streetNeighboursEnricherService = new StreetNeighboursEnricherService();
    private SurroundingEnricherService aroundEnricherService = new SurroundingEnricherService();
    private StreetSplitService streetSplitService = new StreetSplitService();

    // One degree is about 111km
    private static final double METERS_IN_DEGREE = 111_000;

    public void execute() {
        try {
            final var distinctStreetNames = dataSource.findDistinctStreetNames();
            distinctStreetNames.parallelStream().forEach(street -> {
                try {
                    final var addresses = dataSource.findByStreetName(street);
                    final var addressGroups = streetSplitService.split(addresses, maxDistanceToSameStreetNeighboursMeters / METERS_IN_DEGREE);
                    for (Set<Address> addressGroup: addressGroups) {
                        streetNeighboursEnricherService.enrichAddresses(addressGroup);
                        var unresolved = addressGroup.stream().filter(address -> StringUtils.isEmpty(address.zipCode())).collect(Collectors.toSet());
                        for (Address address : unresolved) {
                            final var area = dataSource.findSquareByCoordinates(address.latitude(), address.longitude(), searchAroundMeters / METERS_IN_DEGREE);
                            aroundEnricherService.enrichAddresses(area);
                        }
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
