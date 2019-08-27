package org.test.enricher.model;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(fluent = true, chain = true)
public class BuildingData {
    private Address address;
    private int houseNumber;

    public BuildingData(Address address) {
        this.address = address;
        houseNumber = parseHouseNumber(address.houseNumber());
    }

    private int parseHouseNumber(String houseNumber) {
        Preconditions.checkArgument(StringUtils.isNoneEmpty(houseNumber), "building number should not be empty");
        if (StringUtils.isNumeric(houseNumber)) {
            return Integer.parseInt(houseNumber);
        } else {
            final String[] split = StringUtils.split(houseNumber, "-;");
            return Integer.parseInt(split[0]);
        }
    }
}
