package org.test.enricher;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.test.enricher.model.Address;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AroundEnricher implements Enricher {

    public void enrichAddresses(Set<Address> addressesAround) {
        Set<Address> unknownAddresses = addressesAround
                .stream()
                .filter(address -> StringUtils.isEmpty(address.zipCode()))
                .collect(Collectors.toSet());
        Set<Address> knownAddresses = Sets.newHashSet(CollectionUtils.subtract(addressesAround, unknownAddresses));
        if (CollectionUtils.isEmpty(unknownAddresses)) {
            return;
        }
        final HashSet<Triangle> zipCodeTriangles = Sets.newHashSet();
        final HashSet<Triangle> cityTriangles = Sets.newHashSet();
        generateZipAndCityTriangles(knownAddresses, zipCodeTriangles, cityTriangles);
        for (Address address : unknownAddresses) {
            final var toResolve = new Point(address);
            if (resolveZip(zipCodeTriangles, toResolve)) {
                log.info("resolved zip for building: {}", address.id());
            }
            if (resolveCity(cityTriangles, toResolve)) {
                log.info("resolved city for building: {}", address.id());
            }

        }
    }

    private boolean resolveZip(Set<Triangle> zipTriangles, Point addressToResolve) {
        for (Triangle triangle : zipTriangles) {
            if (isInsideTriangle(triangle, addressToResolve)) {
                addressToResolve.address().zipCode(triangle.zipCode());
                return true;
            }
        }
        return false;
    }

    private boolean resolveCity(Set<Triangle> cityTriangles, Point addressToResolve) {
        for (Triangle triangle : cityTriangles) {
            if (isInsideTriangle(triangle, addressToResolve)) {
                addressToResolve.address().city(triangle.city());
                return true;
            }
        }
        return false;
    }

    private void generateZipAndCityTriangles(Set<Address> knownAddresses, Set<Triangle> zipCodesTriangles, Set<Triangle> cityTriangles) {
        Preconditions.checkNotNull(zipCodesTriangles, "zip codes triangles may not be null");
        Preconditions.checkNotNull(cityTriangles, "city triangles may not be null");
        final Address[] knownAddressesArray = knownAddresses.toArray(new Address[0]);
        for (int i = 0; i < knownAddressesArray.length - 2; i++) {
            final var pointA = new Point(knownAddressesArray[i]);
            for (int j = i + 1; j < knownAddressesArray.length - 1; j++) {
                final var pointB = new Point(knownAddressesArray[j]);
                for (int k = j + 1; k < knownAddressesArray.length; k++) {
                    final var pointC = new Point(knownAddressesArray[k]);
                    if (isSameValue(pointA.zipCode(), pointB.zipCode(), pointC.zipCode())) {
                        zipCodesTriangles.add(new Triangle(pointA, pointB, pointC));
                    }
                    if (isSameValue(pointA.city(), pointB.city(), pointC.city())) {
                        cityTriangles.add(new Triangle(pointA, pointB, pointC));
                    }
                }
            }
        }
    }

    private boolean isSameValue(String value1, String value2, String value3) {
        return StringUtils.equals(value1, value2) &&
                StringUtils.equals(value1, value3) &&
                StringUtils.equals(value2, value3);
    }

    private boolean isInsideTriangle(Triangle triangle, Point pointToCheck) {
        return isInside(triangle.a(), triangle.b(), triangle.c(), pointToCheck);
    }

    /* A function to check whether point P(x, y) lies
       inside the triangle formed by A(x1, y1),
       B(x2, y2) and C(x3, y3) */
    private boolean isInside(Point a, Point b, Point c, Point pointToCheck) {
        /* Calculate area of triangle ABC */
        double abc = area(a, b, c);

        /* Calculate area of triangle PBC */
        double pbc = area(pointToCheck, b, c);

        /* Calculate area of triangle PAC */
        double pac = area(a, pointToCheck, c);

        /* Calculate area of triangle PAB */
        double pab = area(a, b, pointToCheck);

        /* Check if sum of A1, A2 and a3 is same as A */
        return abc == pbc + pac + pab;
    }

    private double area(Point p1, Point p2, Point p3) {
        return Math.abs((p1.x() * (p2.y() - p3.y()) + p2.x() * (p3.y() - p1.y()) + p3.x() * (p1.y() - p2.y())) / 2.0);
    }

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = true, chain = true)
    private class Point {
        private double x;
        private double y;
        private Address address;

        Point(Address address) {
            this.x = address.latitude();
            this.y = address.longitude();
            this.address = address;
        }

        String zipCode() {
            return address.zipCode();
        }

        String city() {
            return address.city();
        }
    }

    @Getter
    @Accessors(fluent = true, chain = true)
    private class Triangle {
        private Point a;
        private Point b;
        private Point c;

        Triangle(Point a, Point b, Point c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        String zipCode() {
            return a.zipCode();
        }

        String city() {
            return a.address().city();
        }
    }
}
