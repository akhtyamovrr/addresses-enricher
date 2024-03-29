package org.test.enricher.datasource;

import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.test.enricher.model.Address;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Strings.nullToEmpty;

public class TSVAddressesDataSource implements AddressesDataSource {
    private final String filePath;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public TSVAddressesDataSource(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Set<Address> findAll() throws Exception {
        Set<Address> addresses = Sets.newHashSet();
        for(CSVRecord record: getCsvRecords()) {
            String street = nullToEmpty(record.get("street")).toUpperCase();
            String houseNumber = record.get("house_number");
            if (isEmptyAddress(street, houseNumber)) {
                continue;
            }
            final Address address = createAddressFromRecord(record, street, houseNumber);
            addresses.add(address);
        }
        return addresses;
    }

    private Address createAddressFromRecord(CSVRecord record, String street, String houseNumber) {
        final var address = new Address().street(street).houseNumber(houseNumber);
        address.id(Long.parseLong(record.get("id")));
        address.latitude(Double.parseDouble(record.get("lat")));
        address.longitude(Double.parseDouble(record.get("lon")));
        address.country(record.get("country"));
        address.state(record.get("state"));
        address.zipCode(record.get("zip_code"));
        address.city(nullToEmpty(record.get("city")).toUpperCase());
        return address;
    }

    @Override
    public Set<String> findDistinctStreetNames() throws Exception {
        return StreamSupport
                .stream(getCsvRecords().spliterator(), false)
                .map(record -> record.get("street"))
                .filter(StringUtils::isNoneEmpty)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Address> findByStreetName(String streetName) throws Exception {
        final Set<Address> addresses = Sets.newHashSet();
        for (CSVRecord record : getCsvRecords()) {
            String street = nullToEmpty(record.get("street")).toUpperCase();
            String houseNumber = record.get("house_number");
            if (isEmptyAddress(street, houseNumber) ||
                    !StringUtils.equalsIgnoreCase(streetName, street)) {
                continue;
            }
            final Address address = createAddressFromRecord(record, street, houseNumber);
            addresses.add(address);
        }
        return addresses;
    }

    @Override
    public Set<Address> findSquareByCoordinates(double latitude, double longitude, double distance) throws Exception {
        final Set<Address> addresses = Sets.newHashSet();
        var minLatitude = latitude - distance;
        var maxLatitude = latitude + distance;
        var minLongitude = longitude - distance;
        var maxLongitude = longitude + distance;
        Iterable<CSVRecord> records = getCsvRecords();
        for (CSVRecord record : records) {
            double lat = Double.parseDouble(record.get("lat"));
            double lon = Double.parseDouble(record.get("lon"));
            if (lat < minLatitude || lat > maxLatitude || lon < minLongitude || lon > maxLongitude) {
                continue;
            }
            String street = nullToEmpty(record.get("street")).toUpperCase();
            String houseNumber = record.get("house_number");
            if (isEmptyAddress(street, houseNumber)) {
                continue;
            }
            final var address = new Address().latitude(lat).longitude(lon).street(street).houseNumber(houseNumber);
            address.id(Long.parseLong(record.get("id")));
            address.country(record.get("country"));
            address.state(record.get("state"));
            address.zipCode(record.get("zip_code"));
            address.city(nullToEmpty(record.get("city")).toUpperCase());
            addresses.add(address);
        }
        return addresses;
    }

    private boolean isEmptyAddress(String street, String houseNumber) {
        return StringUtils.isEmpty(street) || StringUtils.isEmpty(houseNumber);
    }

    private Iterable<CSVRecord> getCsvRecords() throws IOException {
        try {
            lock.readLock().lock();
            Reader in = new FileReader(filePath);
            return CSVFormat.TDF
                    .withHeader("id", "lat", "lon", "country", "state", "zip_code", "city", "street", "house_number")
                    .parse(in);
        } finally {
            lock.readLock().unlock();
        }

    }
}
